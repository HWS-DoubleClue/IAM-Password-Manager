package com.doubleclue.portaldemo.radius;

public enum RadiusPacketType {

	ACCESS_REQUEST(1), ACCESS_ACCEPT(2), ACCESS_REJECT(3), ACCOUNTING_REQUEST(4), ACCOUNTING_RESPONSE(
			5), ACCOUNTING_STATUS(6), PASSWORD_REQUEST(7), PASSWORD_ACCEPT(8), PASSWORD_REJECT(9), ACCOUNTING_MESSAGE(
					10), ACCESS_CHALLENGE(11), STATUS_SERVER(12), STATUS_CLIENT(13), DISCONNECT_REQUEST(40), // RFC
																												// 2882
	DISCONNECT_ACK(41), DISCONNECT_NAK(42), COA_REQUEST(43), COA_ACK(44), COA_NAK(45), STATUS_REQUEST(
			46), STATUS_ACCEPT(47), STATUS_REJECT(48), RESERVED(255);

	int type;

	RadiusPacketType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public static RadiusPacketType find (int type) {
		for (RadiusPacketType packetType: values()) {
			if (packetType.type == type) {
				return packetType;
			}
		}
		return null;
	}


}
