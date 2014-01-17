package com.yayo.warriors.module.chat.model;

import com.yayo.common.utility.Splitable;

public class Channel {

	/** 频道信息 */
	private int channel;
	
	private int subChannel;

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public int getSubChannel() {
		return subChannel;
	}

	public void setSubChannel(int subChannel) {
		this.subChannel = subChannel;
	}

	public static Channel valueOf(int channel) {
		Channel channels = new Channel();
		channels.channel = channel;
		return channels;
	}
	
	public static Channel valueOf(String channelKey) {
		String[] array = channelKey.split(Splitable.ATTRIBUTE_SPLIT);
		Channel channel = new Channel();
		channel.channel = Integer.valueOf(array[0]);
		channel.subChannel = Integer.valueOf(array[1]);
		return channel;
	}
	
	public static Channel valueOf(int channel, int subChannel) {
		Channel channels = new Channel();
		channels.channel = channel;
		channels.subChannel = subChannel;
		return channels;
	}

	@Override
	public String toString() {
		return new StringBuffer().append(channel).append(Splitable.ATTRIBUTE_SPLIT).append(subChannel).toString();
	}
	
	
}
