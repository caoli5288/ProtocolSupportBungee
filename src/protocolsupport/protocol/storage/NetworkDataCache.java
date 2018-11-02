package protocolsupport.protocol.storage;

import gnu.trove.set.hash.TLongHashSet;
import io.netty.util.internal.RecyclableArrayList;
import net.md_5.bungee.protocol.packet.Handshake;
import org.apache.commons.lang3.Validate;
import protocolsupport.api.Connection;

import java.util.LinkedList;
import java.util.UUID;

public class NetworkDataCache {

	private static final String METADATA_KEY = "__PSB_NDC";

	public void storeIn(Connection connection) {
		connection.addMetadata(METADATA_KEY, this);
	}

	private Handshake serverHandshake;

	public void setServerHandshake(Handshake serverHandshake) {
		this.serverHandshake = serverHandshake;
	}

	public Handshake getServerHandshake() {
		return serverHandshake;
	}

	protected String locale = "en_us";

	public void setLocale(String locale) {
		Validate.notNull(locale, "Client locale can't be null");
		this.locale = locale.toLowerCase();
	}

	public String getLocale() {
		return locale;
	}

	private UUID peClientUUID;

	public void setPEClientUUID(UUID uuid) {
		Validate.notNull(uuid, "PE client uuid (identity) can't be null");
		this.peClientUUID = uuid;
	}

	public UUID getPEClientUUID() {
		return peClientUUID;
	}

	private final TLongHashSet watchedEntities = new TLongHashSet(1 << 10);

	public TLongHashSet getWatchedEntities() {
		return watchedEntities;
	}

	public void addWatchedEntity(long id) {
		watchedEntities.add(id);
	}

	public void removeWatchedEntity(long id) {
		watchedEntities.remove(id);
	}

	private LinkedList<RecyclableArrayList> dimUpdateQueue = new LinkedList<>();

	public void dimUpdate(RecyclableArrayList list) {
		dimUpdateQueue.add(list);
	}

	public RecyclableArrayList dimUpdate() {
		return dimUpdateQueue.poll();
	}

	public boolean dimQueue() {
		return !dimUpdateQueue.isEmpty();
	}

	private boolean yFakeFlag;

	public int updateFakeY() {
		return (yFakeFlag = !yFakeFlag) ? 20 : 30;
	}

	private boolean awaitDimensionAck;

	public boolean isAwaitDimensionAck() {
		return awaitDimensionAck;
	}

	public void setAwaitDimensionAck(boolean awaitDimensionAck) {
		this.awaitDimensionAck = awaitDimensionAck;
	}

	private LinkedTokenBasedThrottler<Long> entityKillThrottler = new LinkedTokenBasedThrottler<>(0x40);

	public LinkedTokenBasedThrottler<Long> getEntityKillThrottler() {
		return entityKillThrottler;
	}

	public static NetworkDataCache getFrom(Connection connection) {
		return (NetworkDataCache) connection.getMetadata(METADATA_KEY);
	}
}
