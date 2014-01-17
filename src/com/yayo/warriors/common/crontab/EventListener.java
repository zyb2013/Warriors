package com.yayo.warriors.common.crontab;

public interface EventListener {
	public String cronExpress();
	public String eventName();
	public void execute();
}
