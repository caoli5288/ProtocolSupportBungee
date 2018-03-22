package protocolsupport.protocol.utils;

import gnu.trove.map.hash.TIntObjectHashMap;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;

import java.util.Arrays;

public class ProtocolVersionsHelper {

	private static final TIntObjectHashMap<ProtocolVersion> byOldProtocolId = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<ProtocolVersion> byNewProtocolId = new TIntObjectHashMap<>();
	static {
		Arrays.stream(ProtocolVersion.getAllBeforeI(ProtocolVersion.MINECRAFT_1_6_4)).forEach(version -> byOldProtocolId.put(version.getId(), version));
		Arrays.stream(ProtocolVersion.getAllAfterI(ProtocolVersion.MINECRAFT_1_7_5)).forEach(version -> byNewProtocolId.put(version.getId(), version));
	}

	public static ProtocolVersion getOldProtocolVersion(int protocolid) {
		ProtocolVersion version = byOldProtocolId.get(protocolid);
		return version != null ? version : ProtocolVersion.MINECRAFT_LEGACY;
	}

	public static ProtocolVersion getNewProtocolVersion(int protocolid) {
		ProtocolVersion version = byNewProtocolId.get(protocolid);
		return version != null ? version : ProtocolVersion.MINECRAFT_FUTURE;
	}

	public static final ProtocolVersion LATEST_PC = ProtocolVersion.getLatest(ProtocolType.PC);

}
