package com.doubleclue.dcem.core.cluster;

import com.hazelcast.instance.DefaultNodeExtension;
import com.hazelcast.instance.Node;
import com.hazelcast.internal.cluster.impl.JoinMessage;

public class DcemNoteExtension extends DefaultNodeExtension {

	public DcemNoteExtension(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void validateJoinRequest(JoinMessage joinMessage) {
		super.validateJoinRequest(joinMessage);
		
		System.out.println("DcemNoteExtension.validateJoinRequest()");
	}

}
