package protocolsupport.protocol.packet.handler;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.http.HttpInitializer;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.api.events.PlayerLoginFinishEvent;
import protocolsupport.api.events.PlayerLoginStartEvent;
import protocolsupport.api.events.PlayerPropertiesResolveEvent;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.middleimpl.readable.handshake.v_pe.LoginHandshakePacket;
import protocolsupport.protocol.storage.NetworkDataCache;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PSInitialHandler extends InitialHandler {

	protected ChannelWrapper channel;
	protected Connection connection;
	protected LoginState state = LoginState.HELLO;
	protected UUID uuid;
	protected String username;
	protected EncryptionRequest request;
	protected LoginResult loginProfile;
	protected boolean onlineMode = BungeeCord.getInstance().config.isOnlineMode();
	protected boolean useOnlineModeUUID = onlineMode;
	protected UUID forcedUUID;
	protected UUID offlineuuid;

	public PSInitialHandler(BungeeCord bungee, ListenerInfo listener) {
		super(bungee, listener);
	}

	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		super.connected(channel);
		this.channel = channel;
		this.connection = ConnectionImpl.getFromChannel(channel.getHandle());
	}

	public ChannelWrapper getChannelWrapper() {
		return channel;
	}

	@Override
	public void handle(LoginRequest loginRequest) throws Exception {
		Preconditions.checkState(state == LoginState.HELLO, "Not expecting USERNAME");
		state = LoginState.ONLINEMODERESOLVE;
		this.loginRequest = loginRequest;
		username = loginRequest.getData();
		if (getName().contains(".")) {
			disconnect(BungeeCord.getInstance().getTranslation("name_invalid"));
			return;
		}
		if (getName().length() > 16) {
			disconnect(BungeeCord.getInstance().getTranslation("name_too_long"));
			return;
		}
		int limit = BungeeCord.getInstance().config.getPlayerLimit();
		if ((limit > 0) && (BungeeCord.getInstance().getOnlineCount() > limit)) {
			disconnect(BungeeCord.getInstance().getTranslation("proxy_full"));
			return;
		}
		if (!isOnlineMode() && (BungeeCord.getInstance().getPlayer(getUniqueId()) != null)) {
			disconnect(BungeeCord.getInstance().getTranslation("already_connected_proxy"));
			return;
		}
		BungeeCord.getInstance().getPluginManager().callEvent(new PreLoginEvent(this, (result, error) -> {
            if (result.isCancelled()) {
                disconnect(result.getCancelReasonComponents());
                return;
            }
            if (!isConnected()) {
                return;
            }
            processLoginStart();
        }));
	}

	private LoginRequest loginRequest;

	@Override
	public LoginRequest getLoginRequest() {
		return loginRequest;
	}

	@Override
	public String getName() {
		return username;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public void setUniqueId(UUID uuid) {
		Preconditions.checkState((state == LoginState.HELLO) || (state == LoginState.ONLINEMODERESOLVE), "Can only set uuid while state is username");
		Preconditions.checkState(!isOnlineMode(), "Can only set uuid when online mode is false");
		this.uuid = uuid;
	}

	@Override
	public boolean isOnlineMode() {
		return onlineMode;
	}

	@Override
	public void setOnlineMode(boolean onlineMode) {
		Preconditions.checkState((state == LoginState.HELLO) || (state == LoginState.ONLINEMODERESOLVE), "Can only set uuid while state is username");
		this.onlineMode = onlineMode;
	}

	protected void processLoginStart() {
		PlayerLoginStartEvent event = new PlayerLoginStartEvent(connection, username, isOnlineMode(), getHandshake().getHost());
		ProxyServer.getInstance().getPluginManager().callEvent(event);
		if (event.isLoginDenied()) {
			disconnect(event.getDenyLoginMessage());
			return;
		}

		onlineMode = event.isOnlineMode();
		useOnlineModeUUID = event.useOnlineModeUUID();
		forcedUUID = event.getForcedUUID();

		switch (connection.getVersion().getProtocolType()) {
			case PC: {
				if (isOnlineMode()) {
					state = LoginState.KEY;
					unsafe().sendPacket((request = EncryptionUtil.encryptRequest()));
				} else {
					finishLogin();
				}
				break;
			}
			case PE: {
				if (isOnlineMode()) {
					String xuid = (String) connection.getMetadata(LoginHandshakePacket.XUID_METADATA_KEY);
					if (xuid == null) {
						disconnect("This server is in online mode, but no valid XUID was found (XBOX live auth required)");
						return;
					} else {
//						uuid = new UUID(0, Long.parseLong(xuid));
						uuid = NetworkDataCache.getFrom(connection).getPEClientUUID();
					}
				}
				finishLogin();
				break;
			}
			default: {
				throw new IllegalArgumentException(MessageFormat.format("Unknown protocol type {0}", connection.getVersion().getProtocolType()));
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected void finishLogin() {
		offlineuuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(StandardCharsets.UTF_8));
		if ((isOnlineMode() && !useOnlineModeUUID) || (uuid == null)) {
			uuid = offlineuuid;
		}
		if (forcedUUID != null) {
			uuid = forcedUUID;
		}

		PlayerPropertiesResolveEvent propResolveEvent = new PlayerPropertiesResolveEvent(
			connection, username,
			loginProfile != null && loginProfile.getProperties() != null ?
			Arrays.stream(loginProfile.getProperties())
			.map(bprop -> new ProfileProperty(bprop.getName(), bprop.getValue(), bprop.getSignature()))
			.collect(Collectors.toList())
			: Collections.emptyList()
		);
		BungeeCord.getInstance().getPluginManager().callEvent(propResolveEvent);
		loginProfile = new LoginResult(
			getName(), getUUID(),
			propResolveEvent.getProperties().values().stream()
			.map(psprop -> new LoginResult.Property(psprop.getName(), psprop.getValue(), psprop.getSignature()))
			.collect(Collectors.toList()).toArray(new LoginResult.Property[0])
		);

		if (isOnlineMode()) {
			ProxiedPlayer oldName = BungeeCord.getInstance().getPlayer(getName());
			if (oldName != null) {
				oldName.disconnect(BungeeCord.getInstance().getTranslation("already_connected_proxy"));
			}
			ProxiedPlayer oldID = BungeeCord.getInstance().getPlayer(getUniqueId());
			if (oldID != null) {
				oldID.disconnect(BungeeCord.getInstance().getTranslation("already_connected_proxy"));
			}
		} else {
			ProxiedPlayer oldName = BungeeCord.getInstance().getPlayer(getName());
			if (oldName != null) {
				disconnect(BungeeCord.getInstance().getTranslation("already_connected_proxy"));
				return;
			}
		}
		Callback<LoginEvent> complete = (result, error) -> {
            if (result.isCancelled()) {
                disconnect(result.getCancelReasonComponents());
                return;
            }
            if (!isConnected()) {
                return;
            }
            channel.getHandle().eventLoop().execute(() -> processLoginFinish());
        };
		BungeeCord.getInstance().getPluginManager().callEvent(new LoginEvent(this, complete));
	}

	@Override
	public String getUUID() {
		return uuid.toString().replaceAll("-", "");
	}

	@SuppressWarnings("deprecation")
	protected void processLoginFinish() {
		if (!channel.isClosing()) {
			UserConnection userCon = new UserConnection(BungeeCord.getInstance(), channel, getName(), PSInitialHandler.this);
			userCon.setCompressionThreshold(BungeeCord.getInstance().config.getCompressionThreshold());
			userCon.init();
			unsafe().sendPacket(new LoginSuccess(getUniqueId().toString(), getName()));
			channel.setProtocol(Protocol.GAME);

			PlayerLoginFinishEvent loginFinishEvent = new PlayerLoginFinishEvent(connection, getName(), getUniqueId(), isOnlineMode());
			BungeeCord.getInstance().getPluginManager().callEvent(loginFinishEvent);
			if (loginFinishEvent.isLoginDenied()) {
				disconnect(loginFinishEvent.getDenyLoginMessage());
				return;
			}

			channel.getHandle().pipeline().get(HandlerBoss.class).setHandler(new UpstreamBridge(BungeeCord.getInstance(), userCon));
			BungeeCord.getInstance().getPluginManager().callEvent(new PostLoginEvent(userCon));

			ServerInfo server;
			if (BungeeCord.getInstance().getReconnectHandler() != null) {
				server = BungeeCord.getInstance().getReconnectHandler().getServer(userCon);
			} else {
				server = AbstractReconnectHandler.getForcedHost(PSInitialHandler.this);
			}
			if (server == null) {
				server = BungeeCord.getInstance().getServerInfo(getListener().getDefaultServer());
			}
			userCon.connect(server, null, true);
		}
	}

	@Override
	public LoginResult getLoginProfile() {
		return loginProfile;
	}

	@Override
	public void handle(EncryptionResponse encryptResponse) throws Exception {
		Preconditions.checkState(state == LoginState.KEY, "Not expecting ENCRYPT");
		state = LoginState.AUTHENTICATING;
		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);
		BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
		channel.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder(decrypt));
		if (isFullEncryption(connection.getVersion())) {
			BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
			channel.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder(encrypt));
		}
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		for (byte[] bit : new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()}) {
			sha.update(bit);
		}
		String serverId = new BigInteger(sha.digest()).toString(16);
		ValidReq validReq = new ValidReq(getName(), serverId);
		Callback<String> handler = (result, error) -> {
			if (error == null) {
				LoginResult obj = BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);
				if ((obj != null) && (obj.getId() != null)) {
					loginProfile = obj;
					if (obj.getName() == null) {
					    obj.setName(getName());
                    } else {
					    username = obj.getName();
                    }
					uuid = Util.getUUID(obj.getId());
					finishLogin();
					return;
				}
				disconnect(BungeeCord.getInstance().getTranslation("offline_mode_player"));
			} else {
				disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
				BungeeCord.getInstance().getLogger().log(Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error);
			}
		};
		Http.post(ProtocolSupport.getX19Auth(), validReq, channel.getHandle().eventLoop(), handler);
	}

	protected static boolean isFullEncryption(ProtocolVersion version) {
		return (version.getProtocolType() == ProtocolType.PC) && version.isAfterOrEq(ProtocolVersion.MINECRAFT_1_7_5);
	}

	@Override
	public UUID getOfflineId() {
		return offlineuuid;
	}

	public enum LoginState {
		HELLO, ONLINEMODERESOLVE, KEY, AUTHENTICATING;
	}

	@Data
	@AllArgsConstructor
	public static class ValidReq {
		public String username;
		public String serverId;
	}

	static class Http {

		public static final int TIMEOUT = 5000;
		private static final Cache<String, InetAddress> addressCache = CacheBuilder.newBuilder().expireAfterWrite( 1, TimeUnit.MINUTES ).build();

		@SuppressWarnings("UnusedAssignment")
		public static void get(String url, EventLoop eventLoop, final Callback<String> callback)
		{
			httpRequest(url, null, eventLoop, callback);
		}

		private static void httpRequest(String url, final Object data, EventLoop eventLoop, final Callback<String> callback)
		{
			Preconditions.checkNotNull( url, "url" );
			Preconditions.checkNotNull( eventLoop, "eventLoop" );
			Preconditions.checkNotNull( callback, "callBack" );

			final URI uri = URI.create( url );

			Preconditions.checkNotNull( uri.getScheme(), "scheme" );
			Preconditions.checkNotNull( uri.getHost(), "host" );
			boolean ssl = uri.getScheme().equals( "https" );
			int port = uri.getPort();
			if ( port == -1 )
			{
				switch ( uri.getScheme() )
				{
					case "http":
						port = 80;
						break;
					case "https":
						port = 443;
						break;
					default:
						throw new IllegalArgumentException( "Unknown scheme " + uri.getScheme() );
				}
			}

			InetAddress inetHost = addressCache.getIfPresent( uri.getHost() );
			if ( inetHost == null )
			{
				try
				{
					inetHost = InetAddress.getByName( uri.getHost() );
				} catch ( UnknownHostException ex )
				{
					callback.done( null, ex );
					return;
				}
				addressCache.put( uri.getHost(), inetHost );
			}

			ChannelFutureListener future = new ChannelFutureListener()
			{
				@Override
				public void operationComplete(ChannelFuture future) throws Exception
				{
					if ( future.isSuccess() )
					{
						String path = uri.getRawPath() + ( ( uri.getRawQuery() == null ) ? "" : "?" + uri.getRawQuery() );

						HttpRequest request;
						if (data == null) {
							request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path  );
						} else {
							DefaultFullHttpRequest fullRequest = new DefaultFullHttpRequest (HttpVersion.HTTP_1_1, HttpMethod.POST, path  );
							fullRequest.headers().set( HttpHeaders.Names.CONTENT_TYPE, "application/json");
							String content = BungeeCord.getInstance().gson.toJson(data);
							byte[] raw = content.getBytes("UTF-8");
							fullRequest.headers().set( HttpHeaders.Names.CONTENT_LENGTH, raw.length);
							fullRequest.content().clear().writeBytes(raw);

							request = fullRequest;
						}
						request.headers().set( HttpHeaders.Names.HOST, uri.getHost() );

						future.channel().writeAndFlush( request );
					} else
					{
						addressCache.invalidate( uri.getHost() );
						callback.done( null, future.cause() );
					}
				}
			};

			new Bootstrap().channel( PipelineUtils.getChannel() ).group( eventLoop ).handler( new HttpInitializer( callback, ssl, uri.getHost(), port ) ).
					option( ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT ).remoteAddress( inetHost, port ).connect().addListener( future );
		}

		@SuppressWarnings("UnusedAssignment")
		public static void post(String url, final Object data, EventLoop eventLoop, final Callback<String> callback)
		{
			httpRequest(url, data, eventLoop, callback);
		}

	}

}
