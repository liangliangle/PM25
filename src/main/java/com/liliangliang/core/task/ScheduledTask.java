package com.liliangliang.core.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.liliangliang.core.dao.CityAirDAO;
import com.liliangliang.core.dao.CityDAO;
import com.liliangliang.core.dao.DeviceAirDAO;
import com.liliangliang.core.dao.DeviceDAO;
import com.liliangliang.core.dao.JsonsDAO;
import com.liliangliang.core.entry.City;
import com.liliangliang.envicloud.entry.EnvicloudCityAirLive;
import com.liliangliang.envicloud.entry.EnvicloudCityAllDevice;
import com.liliangliang.envicloud.entry.EnvicloudJsons;
import com.liliangliang.envicloud.util.EnvicloudInfo;
import com.liliangliang.envicloud.util.HttpUtil;
import com.liliangliang.envicloud.util.JsonUtil;

@Component
public class ScheduledTask {
	@Autowired
	private CityDAO cityDAO;
	@Autowired
	private JsonsDAO jsonsDAO;
	@Autowired
	private CityAirDAO cityAirDAO;
	@Autowired
	private DeviceDAO deviceDAO;
	@Autowired
	private DeviceAirDAO deviceAirDAO;

	/**
	 * 每小时获取一次
	 * 
	 * @throws InterruptedException
	 */
	@Scheduled(cron = "0 2 * * * ?")
	public void getCityAir() throws InterruptedException {
		List<City> list = this.cityDAO.getByType(1);
		for (City c : list) {
			String jsons = HttpUtil.HttpGetJson(EnvicloudInfo.BASISURL,
					EnvicloudInfo.VERSION, EnvicloudInfo.CITYAIRLIVE,
					EnvicloudInfo.ACCESSKEY, "/", c.getCitycode());
			this.jsonsDAO.insert(new EnvicloudJsons(jsons,
					EnvicloudInfo.CITYAIRLIVE));
			EnvicloudCityAirLive e = JsonUtil.jsonConver(jsons,
					EnvicloudCityAirLive.class, null);
			if (e.getCitycode() != null && e.getCitycode() != "") {
				this.cityAirDAO.insert(e);
			}
			Thread.sleep(100);// 线程睡眠，减小接口压力
		}
	}

	/**
	 * 每小时获取一次
	 * 
	 * @throws InterruptedException
	 */
	@Scheduled(cron = "0 3 * * * ?")
	public void getDeviceData() throws InterruptedException {
		List<City> list = this.cityDAO.getByType(1);
		for (City c : list) {
			String jsons = HttpUtil.HttpGetJson(EnvicloudInfo.BASISURL,
					EnvicloudInfo.VERSION, EnvicloudInfo.AIR_LIVE_CIRT,
					EnvicloudInfo.ACCESSKEY, "/", c.getCitycode());
			this.jsonsDAO.insert(new EnvicloudJsons(jsons,
					EnvicloudInfo.CITYAIRLIVE));
			EnvicloudCityAllDevice e = JsonUtil.jsonConver(jsons,
					EnvicloudCityAllDevice.class, null);
			try {
				this.deviceAirDAO.insert(e);
				Thread.sleep(1000);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
