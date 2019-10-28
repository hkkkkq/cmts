package com.kthcorp.cmts.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kthcorp.cmts.model.AuthUser;
import com.kthcorp.cmts.model.DicKeywords;
import com.kthcorp.cmts.model.Items;
import com.kthcorp.cmts.model.ItemsTags;
import com.kthcorp.cmts.model.ManualChange;
import com.kthcorp.cmts.model.RelKnowledge;
import com.kthcorp.cmts.service.*;
import com.kthcorp.cmts.util.CommonUtil;
import com.kthcorp.cmts.util.DateUtils;
import com.kthcorp.cmts.util.HttpClientUtil;
import org.apache.poi.hssf.record.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
//@RequestMapping(value = {"", "/dummy"})
@Conditional(CheckApiProfiles.class)
public class ApiController {
	private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

	@Autowired
	private AdminService adminService;
	@Autowired
	private DicService dicService;
	@Autowired
	private ItemsService itemsService;
	@Autowired
	private ApiService apiService;
	@Autowired
	private CcubeService ccubeService;
	@Autowired
	private StatsService statsService;
	@Autowired
	private ItemsTagsService itemsTagsService;
	@Autowired
	private UtilService utilService;
	@Autowired
	private RelKnowledgeService relKnowledgeService;

	//권재일 추가 파일다운로드
	@Autowired
	ResourceLoader resourceLoader;
	
	// #1
	@RequestMapping(value = "/auth/hash", method = RequestMethod.GET)
	@ResponseBody
	public String get__auth_hash(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "authkey", required = false, defaultValue = "sdjnfio2390dsvjklwwe90jf2") String authkey
	) {
		logger.info("#CLOG:API/auth/hash get");

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		try {
			hashcode = apiService.getHashCode(custid, URLDecoder.decode(authkey,"UTF-8"));
			if (!"".equals(hashcode)) {
				rtcode = 1;
				rtmsg = "SUCCESS";
			} else {
				rtcode = -999;
				rtmsg = "HashCode can't get! check AESUtil.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -1;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.addProperty("RESULT", hashcode);

		return result_all.toString();
	}

	// #2
	@RequestMapping(value = "/auth/user/list", method = RequestMethod.GET)
	@ResponseBody
	public String get__auth_user_list(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
	) {
		logger.info("#CLOG:API/auth/user/list get by hash:"+hash);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		List<AuthUser> resultUser = null;
		JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				resultUser = apiService.getAuthUsers();
				if (resultUser != null && resultUser.size() > 0) {
					result1 = new JsonArray();
					for (AuthUser user : resultUser) {
						JsonObject new1 = new JsonObject();
						new1.addProperty("USERID", user.getUserid());
						new1.addProperty("GRANT", user.getUsergrant());
						new1.addProperty("REGDATE", user.getRegdate().toString());
						new1.addProperty("NAME", user.getUsername());
						new1.addProperty("COMPANY", user.getUsercompany());
						new1.addProperty("REGID", user.getRegid());
						new1.addProperty("STAT", user.getStat());
						new1.addProperty("PASSWORD", user.getPassword());

						result1.add(new1);
					}
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Userlist is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}

	// #3
	@RequestMapping(value = "/auth/user/add", method = RequestMethod.POST)
	@ResponseBody
	public String post__auth_user_add(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "target_userid") String target_userid
			, @RequestParam(value = "target_grant") String target_grant
			, @RequestParam(value = "target_company", required = false, defaultValue = "") String target_company
			, @RequestParam(value = "target_name", required = false, defaultValue = "") String target_name
			, @RequestParam(value = "target_password", required = false, defaultValue = "") String target_password
	) {
		logger.info("#CLOG:API/auth/user/add input userid:" + target_userid+"_"+target_grant+"_"+target_company+"_"+target_name+"_"+target_password);
		target_userid = CommonUtil.removeAllSpec2(target_userid);
		target_grant = CommonUtil.removeAllSpec2(target_grant);
		target_company = CommonUtil.removeAllSpec2(target_company);
		target_name = CommonUtil.removeAllSpec2(target_name);

		//String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				AuthUser user = new AuthUser();
				user.setUserid(target_userid);
				user.setUsername(target_name);
				user.setUsergrant(target_grant);
				user.setUsercompany(target_company);
				user.setPassword(target_password);

				int rtins = apiService.insAuthUser(user);
				if (rtins == 1) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "User insert fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}

	// #4
	@RequestMapping(value = "/auth/user/login", method = RequestMethod.POST)
	@ResponseBody
	public String post__auth_user_login(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "userid") String userid
	) {
		logger.info("#CLOG:API/auth/user/login input userid:" + userid);
		userid = CommonUtil.removeAllSpec2(userid);

		//String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		JsonObject result = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				AuthUser req = new AuthUser();
				req.setUserid(userid);

				AuthUser user1 = apiService.getAuthUserById(req);
				if (user1 != null) {

					result = new JsonObject();
					result.addProperty("HASH", hash);
					result.addProperty("GRANT", user1.getUsergrant());
					result.addProperty("PASSWORD", user1.getPassword());
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Not found userid!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result);

		return result_all.toString();
	}

	// #5
	@RequestMapping(value = "/dash/list", method = RequestMethod.GET)
	@ResponseBody
	public String get__dash_list(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
	) {
		logger.info("#CLOG:API/dash/list get");

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		List<AuthUser> resultUser = null;
		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = statsService.getStatsForDash();
				if (result1 != null) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "/dash/list is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}


	// #6
	@RequestMapping(value = "/item/list", method = RequestMethod.POST)
	@ResponseBody
	public String post__item_list(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "pagesize", required = false, defaultValue = "50") String spagesize
			, @RequestParam(value = "pageno", required = false, defaultValue = "1") String spageno

			, @RequestParam(value = "searchtype", required = false, defaultValue = "") String searchType
			, @RequestParam(value = "searchstat", required = false, defaultValue = "") String searchStat
			, @RequestParam(value = "searchsdate", required = false, defaultValue = "") String searchSdate
			, @RequestParam(value = "searchedate", required = false, defaultValue = "") String searchEdate
			, @RequestParam(value = "searchkeyword", required = false, defaultValue = "") String searchKeyword
			, @RequestParam(value = "searchparts", required = false, defaultValue = "") String searchParts
	) {
		logger.info("#CLOG:API/item/list get /pageSize:"+spagesize+"/pageno:"+spageno
		+"/type:"+searchType+"/stat:"+searchStat+"/sdate:"+searchSdate+"/edate:"+searchEdate
		+"/keyword:"+searchKeyword+"/parts:"+searchParts);
		if("".equals(searchKeyword.trim())) searchParts = "";
		searchKeyword = CommonUtil.removeAllSpec2(searchKeyword);

		int pageSize = 0;
		if(!"".equals(spagesize)) pageSize = Integer.parseInt(spagesize);
		int pageNo = 1;
		if(!"".equals(spageno)) pageNo = Integer.parseInt(spageno);

		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getItemsSearch(pageSize, pageNo
										, searchType
										, searchStat
										, searchSdate
										, searchEdate
										, searchKeyword
										, searchParts);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's search-result is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);


		return result_all.toString();
	}

	// #7
	@RequestMapping(value = "/item/upt/one", method = RequestMethod.POST)
	@ResponseBody
	public String post__item_upt_one(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
			, @RequestParam(value = "target_type") String targetType
	) {
		logger.info("#CLOG:API/item/upt/one input itemid:" + itemid +" /type:"+targetType);
		targetType = CommonUtil.removeAllSpec2(targetType);

		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		targetType = targetType.trim();

		int rtcode = -1;
		String rtmsg = "";

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				switch(targetType) {
					case "C": case "A": case "S":
						rtcode = apiService.uptSchedTriggerStatByItemIdxAndType(itemIdx, targetType, "Y" );
						break;
					case "FT": case "RT":
						Items reqIt = new Items();
						reqIt.setIdx(itemIdx);
						reqIt.setStat(targetType);
						rtcode = itemsService.insItemsStat(reqIt);
						break;
				}
				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Item schedTrigger.stat update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}


	// #8
	@RequestMapping(value = "/item/upt/array", method = RequestMethod.POST)
	@ResponseBody
	public String post__item_upt_array(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "items") String items
			, @RequestParam(value = "target_type") String targetType
	) {
		logger.info("#CLOG:API/item/upt/array input items:" + items + "/target_type:" + targetType);
		items = CommonUtil.removeAllSpec2(items);
		targetType = CommonUtil.removeAllSpec2(targetType);

		if(!"".equals(items)) {
			items = items.trim().replace("[","");
			items = items.trim().replace("]","");
		}

		int rtcode = -1;
		String rtmsg = "";

		try {
			//rtcode = apiService.checkAuthByHashCode(custid, hash);
			rtcode = 1;
			if (rtcode == 1) {
				Items req = new Items();
				req.setItemsIdxs(items);
				req.setType(targetType);
				//req.setStat("Y");
				rtcode = itemsService.uptSchedTriggerStatByItemIdxArray(req);
				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items schedTrigger.stat update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);


		return result_all.toString();
	}


	// #9
	@RequestMapping(value = "/pop/movie", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_movie(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") Integer itemid
	) {
		logger.info("#CLOG:API/pop/movie get for itemid:"+itemid);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getMovieInfoByIdx(itemid);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's movieInfo is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}



	// #10
	@RequestMapping(value = "/pop/meta", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_meta(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/meta get for itemid:"+itemid);
		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

				/* pop/meta 조회 시 meta_list type(dup, ext, new) 넣는 조건
  					: 현재 승인대기 이고 이전 승인완료 건 있을 경우 ( 현재 items_tags_keys.stat = Y ), ( 이전 stat = S인 건이 > 0 )
  				*/
				boolean isColorCode = false;
				ItemsTags lastTag = itemsTagsService.getLastTagCntInfo(itemIdx);
				if(lastTag != null && "Y".equals(lastTag.getStat())) {
					lastTag.setStat("S");
					int cntSuccessTagged = itemsTagsService.cntConfirmedTags(lastTag);
					if (cntSuccessTagged > 0) {
						isColorCode = true;
					}
				}
				result1 = itemsTagsService.getItemsMetasByItemIdx(itemIdx, isColorCode);

				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's metas is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}


	// #11
	@RequestMapping(value = "/pop/award", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_award(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") Integer itemid
	) {
		logger.info("#CLOG:API/pop/award get for itemid:"+itemid);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getAwardInfoByIdx(itemid);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's awardInfo is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}


	// #12
	@RequestMapping(value = "/pop/c_cube", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_c_cube(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/c_cube get for itemid:"+itemid);

		int itemIdx = 0;
		if (!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = ccubeService.getCcubeDatasByItemIdx(itemIdx);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's c_cube data is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}


	//#13
	@RequestMapping(value = "/pop/cine21", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_cine21(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/cine21 get for itemid:"+itemid);

		int itemIdx = 0;
		if (!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getCine21DatasByIdx(itemIdx);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's cine21 data is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}


	// #14
	@RequestMapping(value = "/pop/meta/upt/array", method = RequestMethod.POST)
	@ResponseBody
	public String post__pop_meta_upt_array(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "items", required = false, defaultValue = "") String items
			, @RequestParam(value = "duration", required = false, defaultValue = "") String duration
			, @RequestParam(value = "itemid") String itemid
			, @RequestParam(value = "sendnow", required = false, defaultValue = "") String sendnow
	) {
		logger.info("#CLOG:API/pop/meta/upt/array input itemid:"+itemid+"/items:" + items + "/duration:" + duration+"/sendnow:"+sendnow);
		duration = CommonUtil.removeAllSpec2(duration);

		int itemIdx = 0;
		if (!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				int rt = itemsTagsService.changeMetasArraysByTypeFromInputItems(itemIdx, items, duration, sendnow);
				if(rt > 0) {
					rtcode = 1;
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "changeMetasArraysByTypeFromInputItems update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}


	// #15
	@RequestMapping(value = "/pop/meta/restore", method = RequestMethod.POST)
	@ResponseBody
	public String post__pop_meta_restore(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/meta/restore input itemid:" + itemid);

		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		int rtcode = -1;
		String rtmsg = "";

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				rtcode = itemsTagsService.restorePrevTag(itemIdx);

				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Item restorePrevTag update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);


		return result_all.toString();
	}



	// #16
	@RequestMapping(value = "/pop/meta/uptstat", method = RequestMethod.POST)
	@ResponseBody
	public String post__pop_meta_uptstat(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
			, @RequestParam(value = "stat") String stat
	) {
		logger.info("#CLOG:API/pop/meta/uptstat input itemid:" + itemid+"/stat:"+stat);
		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		int rtcode = -1;
		String rtmsg = "";

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				if ("R".equals(stat)) {
                    // 재처리
					rtcode = apiService.uptSchedTriggerStatByItemIdxAndType(itemIdx, "R", "Y");
				} else if ("Y".equals(stat)) {
				    // 승인대기
					Items req = new Items();
					req.setIdx(itemIdx);
					req.setStat("RT");
					rtcode = itemsService.insItemsStat(req);

                    //items_hist에 등록 for 통계
                    Items itemInfo = itemsService.getItemsByIdx(req);
                    String movietitle = "";
                    movietitle = (itemInfo != null && itemInfo.getTitle() != null) ? itemInfo.getTitle().trim() : "";
                    int rthist = itemsService.insItemsHist(req.getIdx(), "meta", "UPT", movietitle, "READY_TAG", itemIdx);
				} else if ("FT".equals(stat)) {
                    Items req = new Items();
                    req.setIdx(itemIdx);
                    req.setStat(stat);
                    rtcode = itemsService.insItemsStat(req);

                    //items_hist에 등록 for 통계
                    Items itemInfo = itemsService.getItemsByIdx(req);
                    String movietitle = "";
                    movietitle = (itemInfo != null && itemInfo.getTitle() != null) ? itemInfo.getTitle().trim() : "";
                    int rthist = itemsService.insItemsHist(req.getIdx(), "meta", "F", movietitle, "FAIL_TAG", itemIdx);
                }
				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Item schedTrigger.stat update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);


		return result_all.toString();
	}



	//#17
	@RequestMapping(value = "/dic/list", method = RequestMethod.GET)
	@ResponseBody
	public String get__dic_list(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "type") String type
			, @RequestParam(value = "pagesize", required = false, defaultValue = "200") String spagesize
			, @RequestParam(value = "KEYWORD", required = false, defaultValue = "") String keyword
			, @RequestParam(value = "pageno") String spageno
			, @RequestParam(value = "orderby", required = false, defaultValue = "new") String orderby
	) {
		logger.info("#CLOG:API/dic/list get for type:"+type+"/keyword:"+keyword+"/orderby:"+orderby+"/pageSize:"+spagesize+"/pageno:"+spageno);	

		int pageSize = 0;
		if(!"".equals(spagesize)) pageSize = Integer.parseInt(spagesize);
        //pageSize = 200;

        type = type.trim().toUpperCase();
		keyword = keyword.trim();

		int pageNo = 0;
		if(!"".equals(spageno)) pageNo = Integer.parseInt(spageno);

		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getDicKeywordsByType(type, keyword, orderby, pageSize, pageNo);	//권재일 추가 07.31 5-1
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Dics data is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}



	// #18
	@RequestMapping(value = "/dic/upt/array", method = RequestMethod.POST)
	@ResponseBody
	public String post__dic_upt_array(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "items") String items
	) {
		logger.info("#CLOG:API/dic/upt/array input items:" + items);

		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Update dic Array update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);


		return result_all.toString();
	}



	// #19
	@RequestMapping(value = "/auth/user/mod", method = RequestMethod.POST)
	@ResponseBody
	public String post__auth_user_mod(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "target_userid") String target_userid
			, @RequestParam(value = "target_grant", required = false) String target_grant
			, @RequestParam(value = "target_company", required = false) String target_company
			, @RequestParam(value = "target_name", required = false) String target_name
			, @RequestParam(value = "target_password", required = false) String target_password
	) {
		logger.info("#CLOG:API/auth/user/mod input userid:" + target_userid+"_"+target_grant+"_"+target_company+"_"+target_name+"_"+target_password);

		//String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				AuthUser user = new AuthUser();
				user.setUserid(target_userid);
				user.setUsername(target_name);
				user.setUsergrant(target_grant);
				user.setUsercompany(target_company);
				user.setPassword(target_password);

				int rtins = apiService.uptAuthUser(user);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "User update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}


	// #20
	@RequestMapping(value = "/auth/user/del", method = RequestMethod.POST)
	@ResponseBody
	public String post__auth_user_del(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "target_userid") String target_userid
	) {
		logger.info("#CLOG:API/auth/user/del input userid:" + target_userid);

		//String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				AuthUser user = new AuthUser();
				user.setUserid(target_userid);

				int rtins = apiService.delAuthUser(user);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "User delete fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}

	//#21
	@RequestMapping(value = "/social", method = RequestMethod.GET)
	@ResponseBody
	public String get__social(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
	) {
		logger.info("#CLOG:API/social get");

		JsonObject result_all = new JsonObject();

		result_all.addProperty("RT_CODE", 1);
		result_all.addProperty("RT_MSG", "SUCCESS");

		JsonObject result1 = new JsonObject();
		int rtcode = -1;
		String rtmsg = "";

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getSnsTopWordsAndGraph();
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "SnsTopWords data is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}




	// #22
	@RequestMapping(value = "/stat/list", method = RequestMethod.POST)
	@ResponseBody
	public String post__stat_list(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "pagesize", required = false, defaultValue = "50") String spagesize
			, @RequestParam(value = "pageno", required = false, defaultValue = "1") String spageno

			, @RequestParam(value = "searchstat", required = false, defaultValue = "") String searchstat
			, @RequestParam(value = "searchsdate", required = false, defaultValue = "2018-02-26") String searchsdate
			, @RequestParam(value = "searchedate", required = false, defaultValue = "2018-02-28") String searchedate
	) {
		logger.info("#CLOG:API/stat/list get pageno:"+spageno+"/pagesize:"+spagesize+"/stat:"
				+searchstat
				+"/sdate:"+searchsdate+"/edate:"+searchedate);
		int pageSize = 0;
		if(!"".equals(spagesize)) pageSize = Integer.parseInt(spagesize);
		int pageNo = 1;
		if(!"".equals(spageno)) pageNo = Integer.parseInt(spageno);

		int rt_code = -1;
		String rtmsg = "";
		JsonObject result_all = new JsonObject();

		JsonObject result = null;
		try {
			result = statsService.getStatsList(pageSize, pageNo, searchsdate, searchedate, searchstat);
			rt_code = 1;
			rtmsg = "SUCCESS";
		} catch (Exception e) {
			rt_code = -1;
			rtmsg = "ERROR";
			e.printStackTrace();
		}

		result_all.add("RESULT", result);

		result_all.addProperty("RT_CODE", rt_code);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}


	// #23
	@RequestMapping(value = "/pop/meta/del/award", method = RequestMethod.POST)
	@ResponseBody
	public String post__pop_meta_del_award(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/meta/del/award input itemid:" + itemid);

		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		int rtcode = -1;
		String rtmsg = "";

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				rtcode = itemsTagsService.delItemsMetasAward(itemIdx);

				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Item meta award delete fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);


		return result_all.toString();
	}



	// #18
	@RequestMapping(value = "/pop/meta/getsubgenre", method = RequestMethod.POST)
	@ResponseBody
	public String post__pop_meta_subgenre(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "items", required = false, defaultValue = "") String items
			, @RequestParam(value = "itemid") String itemid
	) {
		logger.info("#CLOG:API/pop/meta/subgenre input itemid:" + itemid+"/items:"+items);

		int itemIdx = 0;
		if(!"".equals(itemid)) itemIdx = Integer.parseInt(itemid);

		int rtcode = -1;
		String rtmsg = "";

		JsonArray resultArr = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

				resultArr = itemsTagsService.getMetaSubgenre(itemIdx, items);
				/*
				JsonObject item1 = new JsonObject();
				item1.addProperty("type","");
				item1.addProperty("ratio",0.0);
				item1.addProperty("word","테스트 서브장르1");
				resultArr.add(item1);

				JsonObject item2 = new JsonObject();
				item2.addProperty("type","");
				item2.addProperty("ratio",0.0);
				item2.addProperty("word","테스트 서브장르2");
				resultArr.add(item2);
				*/

				rtcode = 1;

				if(rtcode > 0) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Item getSubgenre fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		if(rtcode > 1) rtcode = 1;
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", resultArr);

		logger.info("#CLOG:API/pop/meta/subgenre:RESULT by input itemid:" + itemid+"/items:"+items+" // "+resultArr.toString());

		return result_all.toString();
	}


	// Danger!!! only for admin, 매우조심!!!
	@RequestMapping(value = "/manual/batchChange", method = RequestMethod.POST)
	@ResponseBody
	public String post__manual_batch(Map<String, Object> model
			, @RequestParam(value = "target_mtype", required = true) String target_mtype
			, @RequestParam(value = "from_keyword", required = true) String from_keyword
			, @RequestParam(value = "to_keyword", required = true) String to_keyword
			, @RequestParam(value = "action", required = true) String action

	) {
		logger.info("#CLOG:API/manual/batchChange input " +
				"target_mtype:" + target_mtype+"/from_keyword:"+from_keyword+"/to_keyword:"+to_keyword+"/action:"+action);
		from_keyword = CommonUtil.removeAllSpec2(from_keyword);
		to_keyword = CommonUtil.removeAllSpec2(to_keyword);

		//String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		// 기존 작업중인 내역 조회 후 있으면 에러 리턴
		ManualChange histOne = itemsTagsService.getManualJobHistLastOne();
		if (histOne == null) {
			try {
				itemsTagsService.processManualTagsMetasChange(target_mtype, from_keyword, to_keyword, action);

				rtcode = 1;

			} catch (Exception e) {
				e.printStackTrace();
				rtcode = -999;
				rtmsg = (e.getCause() != null) ? e.getCause().toString() : "Service got exceptions!";
			}
		} else {
			rtcode = -88;
		}

		rtmsg = apiService.getRtmsg(rtcode);
		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}


	// #SNS_TOP_KEYWORDS result
	@RequestMapping(value = "/sns/topwords", method = RequestMethod.GET)
	@ResponseBody
	public String get__pop_award(Model model) {
		logger.info("#CLOG:API/sns/topwords get date:"+ DateUtils.getLocalDate2());

		String hashcode = "";
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			result1 = apiService.getSnsTopKeywords();
			if(result1 != null) {
				rtcode = 1;
				rtmsg = "SUCCESS";
			} else {
				rtcode = -1;
				rtmsg = "SnsTopKeywords is null!";
			}
			//rtmsg = apiService.getRtmsg(rtcode);

		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);

		return result_all.toString();
	}

	@RequestMapping(value="/naver/kordic", method=RequestMethod.GET)
	@ResponseBody
	public String crawl_naverkordic_api(Model model
			, @RequestParam(value="query", required=true) String query
	) {
		logger.debug("#/naver/kordic :: by query:" + query);
		String result = null;
		try {
			result = apiService.getNaverKordicResult(query);
		} catch (Exception e) {

			logger.error("/naver/kordic ERROR:"+e.toString());
			e.printStackTrace();
		}

		return result;
	}
	
	
	//권재일 추가 08.06 03_12 mcid로 동일 컨텐츠 검색 test
	//참고사항 : post__item_list - getItemsSearch
	@RequestMapping(value="/pop/meta/mcidlist", method=RequestMethod.GET)
	@ResponseBody
	public String getItemListSameMcid(Model model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "itemid") int itemid
	) {
		logger.debug("/pop/meta/mcidlist 현 아이템과 동일한 mcid의 컨텐츠 리스트 검색 팝업 표출 - item id = " + itemid);
		String result = null;
		
/*		JsonObject result1 = null;
		
		try {
			result1 = apiService.getItemListSameMcid(itemid);
		} catch (Exception e) {

			logger.error("/pop/meta/mcidlist ERROR:"+e.toString());
			e.printStackTrace();
		}

		return result;
*/
		int rtcode = -1;
		String rtmsg = "";

		JsonObject result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {
				result1 = apiService.getItemListSameMcid(itemid);
				if(result1 != null) {
					rtmsg = "SUCCESS";
				} else {
					rtcode = -1;
					rtmsg = "Items's search-result is null!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);
		result_all.add("RESULT", result1);


		return result_all.toString();
	}
	
	//권재일 추가 해당 카테고리 키워드사전 통쨰로 삭제 (+ 추가)
	@RequestMapping(value = "/dic/del/type", method = RequestMethod.POST)
	@ResponseBody
	public String delDicKeywordsAllByType(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "type") String type
			, @RequestParam(value = "items") String items
	) {
		logger.info("#CLOG:API/dic/del/type input type:" + type);
		int rtcode = -1;
		String rtmsg = "";
		
		DicKeywords dicKeywords = new DicKeywords();
		dicKeywords.setType(type);
		/*
		int rtcode = -1;
		String rtmsg = "";
		//JsonArray result1 = null;

		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Update dic Array update fail!";
				}
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}
		*/
		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

//				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				int rtins1 = dicService.delDicKeywordsAllByType(dicKeywords);
				/*
				if (rtins > 0) {
					//rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Update dic from CSV fail!";
				}
				*/
				
				System.out.println("rtins1 = " + rtins1);
				
				//바로 dic 저장 /dic/upt/array
				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Update dic Array update fail!";
				}
				
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		}catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}
		

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}
	
	//권재일 추가 연관지식 통쨰로 삭제 후 추가
	@RequestMapping(value = "/relknowledge/delete/type", method = RequestMethod.POST)
	@ResponseBody
	public String deleteRelKnowledgesByType(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "type") String type
//			, @RequestParam(value = "items") String items
	) {
		logger.info("#CLOG:API/relknowledge/delete/type input type:" + type);
		int rtcode = -1;
		String rtmsg = "";
		
		/*
		DicKeywords dicKeywords = new DicKeywords();
		dicKeywords.setType(type);
		*/
		RelKnowledge relKnowledge = new RelKnowledge();
		relKnowledge.setRelKnowledgeType(type);	//type라는 이름의 컬럼이 있으므로
		
		
		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

//				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				//int rtins1 = dicService.delDicKeywordsAllByType(dicKeywords);	//사전
				int rtins1 = relKnowledgeService.delRelKnowledgesByType(relKnowledge);
				
				System.out.println("rtins1 = " + rtins1);
//				
//				//바로 dic 저장 /dic/upt/array
//				//int rtins = dicService.modifyDicsByTypesFromArrayList(items);
//				relKnowledge.setItems(items);
//				int rtins = relKnowledgeService.addRelKnowledgesByType(relKnowledge);
//				if (rtins > 0) {
//					rtmsg = "SUCCESS";
//
//				} else {
//					rtcode = -1;
//					rtmsg = "Update dic Array update fail!";
//				}
				
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		}catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}
		

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}
	
	//권재일 추가 연관지식 통쨰로 삭제 후 추가
	@RequestMapping(value = "/relknowledge/upload/type", method = RequestMethod.POST)
	@ResponseBody
	public String uploadRelKnowledgesByType(Map<String, Object> model
			, @RequestParam(value = "custid", required = false, defaultValue = "ollehmeta") String custid
			, @RequestParam(value = "hash", required = false, defaultValue = "hash") String hash
			, @RequestParam(value = "type") String type
			, @RequestParam(value = "items") String items
	) {
		logger.info("#CLOG:API/relknowledge/upload/type input type:" + type);
		System.out.println(" items = " + items);
		int rtcode = -1;
		String rtmsg = "";
		
		/*
		DicKeywords dicKeywords = new DicKeywords();
		dicKeywords.setType(type);
		*/
		RelKnowledge relKnowledge = new RelKnowledge();
		relKnowledge.setRelKnowledgeType(type);	//type라는 이름의 컬럼이 있으므로
		
		
		try {
			rtcode = apiService.checkAuthByHashCode(custid, hash);
			if (rtcode == 1) {

//				int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				//int rtins1 = dicService.delDicKeywordsAllByType(dicKeywords);	//사전
//				int rtins1 = relKnowledgeService.delRelKnowledgesByType(relKnowledge);
//				
//				System.out.println("rtins1 = " + rtins1);
				
				//바로 dic 저장 /dic/upt/array
				//int rtins = dicService.modifyDicsByTypesFromArrayList(items);
				relKnowledge.setItems(items);
				int rtins = relKnowledgeService.addRelKnowledgesByType(relKnowledge);
				if (rtins > 0) {
					rtmsg = "SUCCESS";

				} else {
					rtcode = -1;
					rtmsg = "Update dic Array update fail!";
				}
				
			} else {
				rtmsg = apiService.getRtmsg(rtcode);
			}
		}catch (Exception e) {
			e.printStackTrace();
			rtcode = -999;
			rtmsg = (e.getCause() != null) ? e.getCause().toString(): "Service got exceptions!";
		}
		

		JsonObject result_all = new JsonObject();
		result_all.addProperty("RT_CODE", rtcode);
		result_all.addProperty("RT_MSG", rtmsg);

		return result_all.toString();
	}
	
	//연관지식 다운로드 권재일 추가
	@RequestMapping(value="/relknowledge/download/type")//, method=RequestMethod.POST
	public String downloadRelKnowledgesByType(Map<String, Object> model
			, @RequestParam(value="type", required=false, defaultValue = "") String type
			, HttpServletRequest request
			, HttpServletResponse response
			) {
    	Calendar calendar = Calendar.getInstance();			//[파일업다운로드]
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		//[파일업다운로드]
        
		JsonObject result = new JsonObject();
		
		System.out.println("#/relknowledge/download/type");	//from /admin/dic/keywords/download
		
		String rtmsg = "";
		String strFilePath = "";
		try {
			//strFilePath = adminService.getDicKeywordsListDownload(type);
			strFilePath = relKnowledgeService.getRelKnowledgeListDownload(type);
			if (strFilePath.length() > 0) {
				System.out.println("[파일업다운로드] " + format.format(new Date()) + " strFilePath = " + strFilePath + "file generated success!!");
				rtmsg = "SUCCESS";
			}
		
		} catch(Exception e) {
			//rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtfile", strFilePath);
		result.addProperty("rtmsg", rtmsg);
		
		System.out.println("[파일업다운로드] " + format.format(new Date()) + " resourceLoader.getResource(\"classpath:static/\") 시도..");
		
		Resource resClasspath;
		String strResClasspath = "";
		resClasspath = resourceLoader.getResource("classpath:static/");
		
		System.out.println("[파일업다운로드] " + format.format(new Date()) + " resClasspath = " + resClasspath);
		System.out.println("[파일업다운로드] " + format.format(new Date()) + " resClasspath.toString() = " + resClasspath.toString());
		System.out.println("[파일업다운로드] " + format.format(new Date()) + " resClasspath.exists() = " + resClasspath.exists());
		System.out.println("[파일업다운로드] " + format.format(new Date()) + " resClasspath.getDescription() = " + resClasspath.getDescription());
		
		FileInputStream fis;
		FileOutputStream fos;
		OutputStream os;
		
		try {
			strResClasspath = resClasspath.getURI().getPath();
			System.out.println("strResClasspath =" + strResClasspath);
			
			//파일 복사 from strFileName to strResClasspath
			String strFileName = strFilePath.substring(strFilePath.lastIndexOf(File.separator)+1);
			System.out.println("Copy from " + strFilePath + " " + strFileName + " to " + strResClasspath);
			
			//파일 복사 from https://blowmj.tistory.com/entry/JAVA-%ED%8C%8C%EC%9D%BC%EC%9D%98-%EB%B3%B5%EC%82%AC-%EC%9D%B4%EB%8F%99-%EC%82%AD%EC%A0%9C-%EC%83%9D%EC%84%B1-%EC%A1%B4%EC%9E%AC%EC%97%AC%EB%B6%80-%ED%99%95%EC%9D%B8
			fis = new FileInputStream(strFilePath);
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " fis = new FileInputStream(~~)");
			fos = new FileOutputStream(strResClasspath + File.separator + strFileName);
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " fos = new FileOutputStream(~~)");
			os = response.getOutputStream();
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " os = response.getOutputStream()");
			
			int data = 0;
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " fos.write(data); 루프문 시작");
			while((data=fis.read())!=-1) {
				fos.write(data);
			}
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " fos.write(data); 루프문 끝");
			
			fis.close();
			fos.close();
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " fis.close() fos.close()");
			
			//String strFilePath
			//다운로드 파일 링크
			String strUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			//os.write(("파일 생성 완료 <a href='"+strUrl+"/"+strFileName+"'>다운로드</a>").getBytes());
			System.out.println("[파일업다운로드] " + format.format(new Date()) + " file url = " + strUrl+"/"+strFileName);
			os.write((strUrl+"/"+strFileName).getBytes());
			os.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return result.toString();
	}
	
}