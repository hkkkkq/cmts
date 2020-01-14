package com.kthcorp.cmts.controller;

import com.google.gson.JsonObject;
import com.kthcorp.cmts.model.*;
import com.kthcorp.cmts.service.*;
import com.kthcorp.cmts.util.CommonUtil;
import com.kthcorp.cmts.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import scala.Char;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
//@RequestMapping(value = {"", "/dummy"})
@Conditional(CheckAdminProfiles.class)
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Value("${cmts.collector.naverblog.search_url}")
	private String naverblog_url;
	@Value("${cmts.collector.daumnews.search_url}")
	private String daumnews_url;
	@Value("${cmts.collector.navernews.search_url}")
	private String navernews_url;
	@Value("${cmts.admin.page_size}")
	private Integer pageSize;

	@Autowired
	private AdminService adminService;
	@Autowired
	private DicService dicService;
	@Autowired
	private ItemsService itemsService;
	@Autowired
	private ApiService apiService;
	
    @Value("${spring.static.resource.location}")
    private String UPLOAD_DIR;
    
	//권재일 추가 파일다운로드
	@Autowired
	ResourceLoader resourceLoader;

	@RequestMapping(value="/admin", method=RequestMethod.GET)
	public ModelAndView home(Model model
			, @RequestParam(value="page", required=false, defaultValue = "1") Integer pageno
			, @RequestParam(value="search", required=false, defaultValue = "") String search
	) {

		logger.debug("#/admin/item/list get");

		if(pageno < 1) { pageno = 1; }
		Items req = new Items();
		req.setPageNo(pageno);
		req.setPageSize(pageSize);
		if(!"".equals(search)) req.setTitle(search);

		List<Items> itemsList = adminService.getItemsList(req);

		/* pagination */
		Integer countItems = adminService.countItems(req);
		Map<String, Object> listPaging = CommonUtil.getPaginationJump(countItems, pageSize, pageno,10 );
		List<String> listActive = null;
		List<Integer> listPage = null;
		if (listPaging != null) {
			listActive = (List<String>) listPaging.get("listActive");
			listPage = (List<Integer>) listPaging.get("listPage");
		}

		ModelAndView mav = new ModelAndView("admin/itemsList");
		mav.addObject("itemsList", itemsList);
		mav.addObject("pageSize", pageSize);
		mav.addObject("countItems", countItems);
		mav.addObject("pageno", pageno);

		mav.addObject("listActive", listActive);
		mav.addObject("listPage", listPage);
		mav.addObject("searchTxt", search);

		return mav;
	}

	@RequestMapping(value="/admin/items/list", method=RequestMethod.GET)
	public ModelAndView itemsList(Map<String, Object> model
			, @RequestParam(value="page", required=false, defaultValue = "1") Integer pageno
			, @RequestParam(value="search", required=false, defaultValue = "") String search
	) {

		logger.debug("#/admin/item/list get");

		if(pageno < 1) { pageno = 1; }
		Items req = new Items();
		req.setPageNo(pageno);
		req.setPageSize(pageSize);
		if(!"".equals(search)) req.setTitle(search);

		List<Items> itemsList = adminService.getItemsList(req);

		/* pagination */
		Integer countItems = adminService.countItems(req);
		Map<String, Object> listPaging = CommonUtil.getPaginationJump(countItems, pageSize, pageno, 10);
		List<String> listActive = null;
		List<Integer> listPage = null;
		if (listPaging != null) {
			listActive = (List<String>) listPaging.get("listActive");
			listPage = (List<Integer>) listPaging.get("listPage");
		}

		ModelAndView mav = new ModelAndView("admin/itemsList");
		mav.addObject("itemsList", itemsList);
		mav.addObject("pageSize", pageSize);
		mav.addObject("countItems", countItems);
		mav.addObject("pageno", pageno);

		mav.addObject("listActive", listActive);
		mav.addObject("listPage", listPage);
		mav.addObject("searchTxt", search);

		return mav;
	}



	@RequestMapping(value="/admin/movie_cine21/list", method=RequestMethod.GET)
	public ModelAndView movieCine21List(Map<String, Object> model
			, @RequestParam(value="page", required=false, defaultValue = "1") Integer pageno
			, @RequestParam(value="search", required=false, defaultValue = "") String search
			) {

		logger.debug("#/admin/movie_cine21/list get");

		if(pageno < 1) { pageno = 1; }
		MovieCine21 req = new MovieCine21();
		req.setPageNo(pageno);
		req.setPageSize(pageSize);
		if(!"".equals(search)) req.setMovieNm(search);

		List<MovieCine21> itemsList = adminService.getMovieCine21(req);

		/* pagination */
		Integer countItems = adminService.cntMovieCine21(req);
		Map<String, Object> listPaging = CommonUtil.getPaginationJump(countItems, pageSize, pageno, 10);
		List<String> listActive = null;
		List<Integer> listPage = null;
		if (listPaging != null) {
			listActive = (List<String>) listPaging.get("listActive");
			listPage = (List<Integer>) listPaging.get("listPage");
		}

		ModelAndView mav = new ModelAndView("admin/cine21List");
		mav.addObject("itemsList", itemsList);
		mav.addObject("pageSize", pageSize);
		mav.addObject("countItems", countItems);
		mav.addObject("pageno", pageno);

		mav.addObject("listActive", listActive);
		mav.addObject("listPage", listPage);

		return mav;
	}


	@RequestMapping(value="/admin/item/{idx}", method=RequestMethod.GET)
	public ModelAndView itemGet(Map<String, Object> model
			, @PathVariable Integer idx ) {

		logger.debug("#/admin/item/"+idx+" get");

		Items req = new Items();
		req.setIdx(idx);
		Items item = adminService.getItemsByIdx(req);

		List<SchedTrigger> schedList = adminService.getSchedTriggerListByItemIdx(idx);

		ModelAndView mav = new ModelAndView("admin/item");
		mav.addObject("item", item);
		mav.addObject("schedList", schedList);
		return mav;
	}


	@RequestMapping(value="/admin/item/content/{type}/{sc_id}", method=RequestMethod.GET)
	public ModelAndView itemGetContent(Map<String, Object> model
			, @PathVariable String type , @PathVariable Integer sc_id ) {

		logger.debug("#/admin/item/content/"+type+"/"+sc_id+" get");

		SchedTargetMappingHist reqh = new SchedTargetMappingHist();
		reqh.setType(type);
		reqh.setSc_id(sc_id);
		List<SchedTargetMappingHist> contentList = adminService.getSchedTriggerListBySc_id(reqh);

		ModelAndView mav = new ModelAndView("admin/contentList");
		mav.addObject("sc_id", sc_id);
		mav.addObject("contentList", contentList);
		return mav;
	}


	@RequestMapping(value="/admin/item/history/{type}/{sc_id}", method=RequestMethod.GET)
	public ModelAndView itemGetHistory(Map<String, Object> model
			, @PathVariable String type , @PathVariable Integer sc_id ) {

		logger.debug("#/admin/item/history/"+type+"/"+sc_id+" get");

		SchedTargetMappingHist reqh = new SchedTargetMappingHist();
		reqh.setType(type);
		reqh.setSc_id(sc_id);
		List<SchedTargetMappingHist> contentList = adminService.getSchedTriggerHistListAll(reqh);

		ModelAndView mav = new ModelAndView("admin/historyList");
		mav.addObject("sc_id", sc_id);
		mav.addObject("contentList", contentList);
		return mav;
	}


	@RequestMapping(value="/admin/properties/list", method=RequestMethod.GET)
	public ModelAndView getPropertiesList(Map<String, Object> model) {

		logger.debug("#/admin/properties/list get");
		ConfTarget req = new ConfTarget();
		req.setPageNo(1);
		req.setPageSize(10);

		List<ConfTarget> result = adminService.getConfTargetList(req);

		System.out.println("#/admin/properties/list result:"+result.toString());

		ModelAndView mav = new ModelAndView("admin/propertiesList");
		mav.addObject("propertiesList", result);
		mav.addObject("naverblog_url", naverblog_url);
		mav.addObject("daumnews_url", daumnews_url);
		mav.addObject("navernews_url", navernews_url);
		return mav;
	}


	@RequestMapping(value="/admin/dic/{type}/list", method=RequestMethod.GET)
	public ModelAndView getDicList(Map<String, Object> model
			, @PathVariable String type) {

		logger.debug("#/admin/dic/"+type+"/list get");


		ModelAndView mav = new ModelAndView("admin/dicList");
		mav.addObject("type", type);

		switch(type) {
			case "filter":
				List<DicFilterWords> filterList = dicService.getDicFilterWords();
				mav.addObject("wordsList", filterList);
				break;
			case "change":
				List<DicChangeWords> changeList = dicService.getDicChangeWords();
				mav.addObject("wordsList", changeList);
				break;
			case "notuse":
				List<DicNotuseWords> notuseList = dicService.getDicNotuseWords();
				mav.addObject("wordsList", notuseList);
				break;
			case "add":
				List<DicAddWords> addList = dicService.getDicAddWords();

				System.out.println("#addList:"+addList.toString());
				mav.addObject("wordsList", addList);
				break;
			case "keywords":
				List<DicKeywords> keywordsList = dicService.getDicKeywordsListAll();

				System.out.println("#keywordsList:"+keywordsList.toString());
				mav.addObject("wordsList", keywordsList);
				break;
			case "tags":
				List<MetaKeywordMapping> tagList = dicService.getMetaKeywordMappingListAll();

				System.out.println("#tagList:"+tagList.toString());
				mav.addObject("wordsList", tagList);
				break;
		}

		return mav;
	}


	@RequestMapping(value="/admin/dic/tags", method=RequestMethod.GET)
	public ModelAndView getDicTagsList(Map<String, Object> model) {

		logger.debug("#/admin/dic/tags get");
		String type = "tags";

		ModelAndView mav = new ModelAndView("admin/tagList");
		mav.addObject("type", type);

		List<MetaKeywordMapping> tagList = dicService.getMetaKeywordMappingListAll();
		System.out.println("#tagList:"+tagList.toString());
		mav.addObject("wordsList", tagList);

		return mav;
	}


	@RequestMapping(value="/admin/dic/keywords", method=RequestMethod.GET)
	public ModelAndView getDicKeywordsList(Map<String, Object> model) {

		logger.debug("#/admin/dic/keywords get");
		String type = "keywords";

		ModelAndView mav = new ModelAndView("admin/keywordsList");
		mav.addObject("type", type);

		List<DicKeywords> keywordsList = dicService.getDicKeywordsListAll();
		System.out.println("#keywordsList:"+keywordsList.toString());
		mav.addObject("wordsList", keywordsList);

		return mav;
	}


	/** HTTP-POST **/
	@RequestMapping(value="/admin/items/add", method=RequestMethod.POST)
	@ResponseBody
	public String addItem(Map<String, Object> model
				, @RequestParam(value="title", required=true) String title ) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/items/add input title:"+title);

			int rtcode = 0;
			String rtmsg = "";
			try {
				Items req = new Items();
				req.setCid("");
				req.setTitle(title);
				req.setRegid("ghkdwo77");
				req.setStat("Y");
				req.setType("movie");
				rtcode = itemsService.insItems(req);

				System.out.println("#/admin/items/add inserted idx:"+req.getIdx());
			} catch(Exception e) {
				rtcode = -999;
				rtmsg = "System fail.";
				e.printStackTrace();
			}
			result.addProperty("rtcode", rtcode);
			result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}

	@RequestMapping(value="/admin/items/del", method=RequestMethod.POST)
	@ResponseBody
	public String delItem(Map<String, Object> model
			, @RequestParam(value="idx", required=true) Integer idx ) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/items/del input idx:"+idx);

		int rtcode = 0;
		String rtmsg = "";
		try {
			Items req = new Items();
			req.setIdx(idx);
			rtcode = itemsService.delItems(req);

			System.out.println("#/admin/items/del deleted idx:"+req.getIdx());
		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}

	@RequestMapping(value="/admin/sched/retry", method=RequestMethod.POST)
	@ResponseBody
	public String uptSchedTriggerRetry(Map<String, Object> model
			, @RequestParam(value="sc_id", required=true) int sc_id ) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/sched/retry by:"+sc_id);

		int rtcode = 0;
		String rtmsg = "";
		try {
			SchedTrigger req = new SchedTrigger();
			req.setSc_id(sc_id);

			rtcode = adminService.uptSchedTriggerRetry(req);
			if (rtcode > 0) rtmsg = "SUCCESS";

		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}
	/*
	@RequestMapping(value="/admin/dic/{type}/list", method=RequestMethod.GET)
	public ModelAndView getPropertiesList(Map<String, Object> model
			, @PathVariable String type) {

		logger.debug("#/admin/dic/"+type+"/list get");


		ModelAndView mav = new ModelAndView("admin/dic/list");
		return mav;
	}


	@RequestMapping(value="/admin/items/add", method=RequestMethod.POST)
	public JsonObject addItem(Map<String, Object> model) {
		JsonObject result = new JsonObject();

			int rtcode = 0;
			String rtmsg = "";
			try {

			} catch(Exception e) {
				rtcode = -999;
				rtmsg = "System fail.";
				e.printStackTrace();
			}
			result.addProperty("rtcode", rtcode);
			result.addProperty("rtmsg", rtmsg);

		return result;
	}
	*/
	@RequestMapping(value="/admin/properties/target/edit", method=RequestMethod.POST)
	@ResponseBody
	public String uptTarget(Map<String, Object> model
			, @RequestParam(value="tg_id", required=true) int tg_id
			, @RequestParam(value="title", required=false) String title
			, @RequestParam(value="descript", required=false) String descript
			, @RequestParam(value="tg_url", required=false) String tg_url
			, @RequestParam(value="tg_url_param1", required=false) String tg_url_param1
			, @RequestParam(value="tg_url_param2", required=false) String tg_url_param2
			, @RequestParam(value="tg_url_param3", required=false) String tg_url_param3
			, @RequestParam(value="param1", required=false) String param1
			, @RequestParam(value="regid", required=false) String regid
			, @RequestParam(value="stat", required=false) String stat
			, @RequestParam(value="fail_count1", required=false) Integer fail_count1
			, @RequestParam(value="fail_count2", required=false) Integer fail_count2
	) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/properties/target/edit:"+tg_id);

		int rtcode = 0;
		String rtmsg = "";
		try {
			ConfTarget req = new ConfTarget();
			req.setTg_id(tg_id);
			req.setTitle(title);
			req.setDescript(descript);
			req.setTg_url(tg_url);
			req.setTg_url_param1(tg_url_param1);
			req.setTg_url_param2(tg_url_param2);
			req.setTg_url_param3(tg_url_param3);
			req.setRegid(regid);
			req.setParam1(param1);
			req.setStat(stat);
			req.setFail_count1(fail_count1);
			req.setFail_count2(fail_count2);

			rtcode = adminService.uptConfTarget(req);
			if (rtcode > 0) rtmsg = "SUCCESS";

		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}

	@RequestMapping(value="/admin/properties/target/remove", method=RequestMethod.POST)
	@ResponseBody
	public String delTarget(Map<String, Object> model
			, @RequestParam(value="tg_id", required=true) int tg_id) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/properties/target/remove:"+tg_id);

		int rtcode = 0;
		String rtmsg = "";
		try {
			ConfTarget req = new ConfTarget();
			req.setTg_id(tg_id);

			rtcode = adminService.delConfTarget(req);
			if (rtcode > 0) rtmsg = "SUCCESS";

		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}


	@RequestMapping(value="/admin/properties/preset/edit", method=RequestMethod.POST)
	@ResponseBody
	public String uptPreset(Map<String, Object> model
			, @RequestParam(value="ps_id", required=true) int ps_id
			, @RequestParam(value="ps_tag", required=false) String ps_tag
			, @RequestParam(value="ps_type", required=false) String ps_type
			, @RequestParam(value="dest_field", required=false) String dest_field
			, @RequestParam(value="ps_add_url", required=false) String ps_add_url
			, @RequestParam(value="descriptp", required=false) String descriptp
			, @RequestParam(value="dest_charset", required=false) String dest_charset
	) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/properties/preset/edit:"+ps_id);

		int rtcode = 0;
		String rtmsg = "";
		try {
			ConfPreset req = new ConfPreset();
			req.setPs_id(ps_id);
			req.setPs_tag(ps_tag);
			req.setPs_type(ps_type);
			req.setDest_field(dest_field);
			req.setPs_add_url(ps_add_url);
			req.setDescriptp(descriptp);
			req.setDest_charset(dest_charset);


			System.out.println("#/admin/properties/preset/edit:"+ps_id+"  // params:"+req.toString());


			rtcode = adminService.uptConfPreset(req);
			if (rtcode > 0) rtmsg = "SUCCESS";

		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}

	@RequestMapping(value="/admin/properties/preset/remove", method=RequestMethod.POST)
	@ResponseBody
	public String delPreset(Map<String, Object> model
			, @RequestParam(value="ps_id", required=true) int ps_id) {
		JsonObject result = new JsonObject();

		System.out.println("#/admin/properties/preset/remove:"+ps_id);

		int rtcode = 0;
		String rtmsg = "";
		try {
			ConfPreset req = new ConfPreset();
			req.setPs_id(ps_id);

			rtcode = adminService.delConfPreset(req);
			if (rtcode > 0) rtmsg = "SUCCESS";

		} catch(Exception e) {
			rtcode = -999;
			rtmsg = "System fail.";
			e.printStackTrace();
		}
		result.addProperty("rtcode", rtcode);
		result.addProperty("rtmsg", rtmsg);

		return result.toString();
	}

	@RequestMapping(value="/admin/manual/change", method=RequestMethod.GET)
	public ModelAndView manual_change(Map<String, Object> model
			, @RequestParam(value="page", required=false, defaultValue = "1") Integer pageno
			, @RequestParam(value="keyword", required=false) String keyword

	) {

		logger.debug("#/admin/manual/change get");
		if(pageno < 1) { pageno = 1; }
		ManualChange req = new ManualChange();
		req.setPageNo(pageno);
		req.setPageSize(pageSize);
		req.setKeyword(keyword);

		List<ManualChange> itemList = adminService.getManualJobHist(req);
		int countItems = adminService.cntManualJobHist();
		Map<String, Object> listPaging = CommonUtil.getPaginationJump(countItems, pageSize, pageno, 10);
		List<String> listActive = null;
		List<Integer> listPage = null;
		if (listPaging != null) {
			listActive = (List<String>) listPaging.get("listActive");
			listPage = (List<Integer>) listPaging.get("listPage");
		}
		ModelAndView mav = new ModelAndView("admin/manualChange");

		mav.addObject("itemsList", itemList);
		mav.addObject("pageSize", pageSize);
		mav.addObject("countItems", countItems);
		mav.addObject("pageno", pageno);

		mav.addObject("listActive", listActive);
		mav.addObject("listPage", listPage);

		return mav;
	}


	@RequestMapping(value="/admin/keywordrank/list", method=RequestMethod.GET)
	public ModelAndView keywordrankList(Map<String, Object> model
			, @RequestParam(value="genre", required=false, defaultValue = "") String genre
			, @RequestParam(value="type", required=false, defaultValue = "") String type
	) {

		logger.debug("#/admin/keywordrank/list get");

		DicKeywords req = new DicKeywords();
		req.setGenre(genre);
		req.setType(type);
		List<DicKeywords> itemsList = dicService.getRankWordsByGenreAndType(req);

		/* pagination */
		Integer countItems = itemsList.size();

		ModelAndView mav = new ModelAndView("admin/keywordrankList");
		mav.addObject("itemsList", itemsList);
		mav.addObject("countItems", countItems);
		mav.addObject("oldgenre", genre);
		mav.addObject("oldtype", type);

		String origGenres = "공포, 다큐멘터리, 서사, 뮤지컬, 무협, 미스터리, 액션, 코미디, SF, 에로, 드라마, 컬트, 판타지, 가족, 전쟁, 멜로로맨스, " +
				"범죄, 모험, 애니메이션, 블랙코미디, 실험, 스릴러, 서스펜스, 느와르, TV영화, 서부, 공연실황";
		origGenres = origGenres.replace(" ","");
		String genresp[] = origGenres.split(",");
		mav.addObject("genres", genresp);

		List<String> types = dicService.getKeywordTypes();
		mav.addObject("types", types);

		return mav;
	}

	@RequestMapping(value="/bypass/naver/kordic", method=RequestMethod.GET)
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

	@RequestMapping(value="/coll/naver/kordic", method=RequestMethod.GET)
	@ResponseBody
	public String coll_naverkordic_api(Model model
			, @RequestParam(value="query", required=true) String query
	) {
		logger.debug("#/naver/kordic :: by query:" + query);
		String result = null;
		try {
			result = apiService.getCollNaverKordicResult(query);
		} catch (Exception e) {

			logger.error("/naver/kordic ERROR:"+e.toString());
			e.printStackTrace();
		}

		return result;
	}

	// CSV 다운로드 by jaeyeon.hwang
	@RequestMapping(value="/admin/dic/keywords/download")
	public ResponseEntity<Resource> getDicKeywordsListDownload_NEW(Map<String, Object> model
			, @RequestParam(value="type", required=false, defaultValue = "") String type
			, HttpServletRequest request
			, HttpServletResponse response
	) {

		String contentType = null;
		String strFilePath = "";
		FileInputStream fis;
		FileOutputStream fos;
		OutputStream os;

		Resource resClasspath;
		String strResClasspath = "";
		resClasspath = resourceLoader.getResource("classpath:static/");

		strFilePath = UPLOAD_DIR + "DIC_KEYWORDS_" + type.toUpperCase() + ".csv";
		logger.debug("#strFilePath = " + strFilePath);

		String strFileName = "";
		String strUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		ByteArrayResource resource = null;

		try {
			//2019.11.20 파일정보 로딩
			DicKeywords fileInfoParam = new DicKeywords();
			String origFileName="DIC_KEYWORDS_" + type.toUpperCase();
			String origFilePath="";
			fileInfoParam.setFilePath(origFileName);
			DicKeywords fileInfoResult = dicService.getCsvFileNameTimestamp(fileInfoParam);
			if (fileInfoResult != null) {
				String strDateTime = fileInfoResult.getRegdate().toString();
				System.out.println("strDateTime = " + strDateTime);
				strDateTime = strDateTime.substring(0, 16).replace("-", "").replace(" ", "_").replace(":", "");
				System.out.println("strDateTime = " + strDateTime);
				strFileName = fileInfoParam.getFilePath() + "_" + strDateTime + ".csv";
				System.out.println("strFileName = " + strFileName);

				origFilePath = strFileName = fileInfoParam.getFilePath() + ".csv";
			} else {
				strFileName = fileInfoParam.getFilePath() + ".csv";

				origFilePath = strFileName = fileInfoParam.getFilePath() + ".csv";
			}

			// copy for new File
			FileUtils.copy(strFilePath, strFileName);

			byte[] data = Files.readAllBytes(Paths.get(strFileName));
			resource = new ByteArrayResource(data);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + strFileName + "\"")
				.body(resource);
	}
	
	//CSV 다운로드 권재일 추가 5_2_1
	@RequestMapping(value="/admin/dic/keywords/download_old")//, method=RequestMethod.POST
	public String getDicKeywordsListDownload(Map<String, Object> model
			, @RequestParam(value="type", required=false, defaultValue = "") String type
			, HttpServletRequest request
			, HttpServletResponse response
			) {
		
		JsonObject result = new JsonObject();
		
		String strFilePath = "";
		FileInputStream fis;
		FileOutputStream fos;
		OutputStream os;
		
		Resource resClasspath;
		String strResClasspath = "";
		resClasspath = resourceLoader.getResource("classpath:static/");
		
		strFilePath = UPLOAD_DIR + "DIC_KEYWORDS_" + type.toUpperCase()+".csv";
		logger.debug("#strFilePath = " + strFilePath);
		
		String strFileName = "";
		String strUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		try {
			//2019.11.20 파일정보 로딩
			DicKeywords fileInfoParam = new DicKeywords();
			fileInfoParam.setFilePath("DIC_KEYWORDS_" + type.toUpperCase());
			DicKeywords fileInfoResult = dicService.getCsvFileNameTimestamp(fileInfoParam);
			if(fileInfoResult!=null) {
				String strDateTime = fileInfoResult.getRegdate().toString();
				System.out.println("strDateTime = " + strDateTime);
				strDateTime = strDateTime.substring(0, 16).replace("-", "").replace(" ", "_").replace(":", "");
				System.out.println("strDateTime = " + strDateTime);
				strFileName = fileInfoParam.getFilePath() + "_" + strDateTime + ".csv";
				System.out.println("strFileName = " + strFileName);
			}else {
				strFileName = fileInfoParam.getFilePath() + ".csv";
			}
			
			//신버전 : 데이터를 그대로 태우기 - 느림

			//response.setContentType("application/vnd.ms-excel; charset=utf-8");
			response.setContentType("application/vnd.ms-excel; charset=iso-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" +
					java.net.URLEncoder.encode(strFileName, "utf-8") + "\";");
			response.setHeader("Content-Transfer-Encoding", "binary");
			//response.reset();

			os = response.getOutputStream();

			fis = new FileInputStream(strFilePath);

			// for FileName
			os.write((strFileName+"::::").getBytes("UTF-8"));

			// for Read by Charset
			int intRead = 0;
			Charset inputCharset = Charset.forName("MS949");
			InputStreamReader isr = new InputStreamReader(fis, inputCharset);
			Reader reader = null;
			reader = new BufferedReader(isr);
			StringBuffer stringBuffer = new StringBuffer();
			while( ( intRead = reader.read() ) > -1 ) {
				stringBuffer.append((char)intRead);
			}

			//String writeString = stringBuffer.toString();
			//ByteBuffer byteBuffer = StandardCharsets.ISO_8859_1.encode(writeString);
			reader.close();



			Writer writer = null;
			//int data = 0;
			//while((data = fis.read()) != -1) {
			//	os.write(data);
			//}

			// for Write by Charset
			writer = new OutputStreamWriter(os, Charset.forName("MS949"));

			String writeStr = stringBuffer.toString();
			byte[] ptext = writeStr.getBytes(Charset.forName("MS949"));
			String toValue = new String(ptext, Charset.forName("MS949"));

			logger.info("#MLOG Write Buffer to Response::"+ toValue.toString());

			//String writeStr = byteBuffer.toString();
			writer.write(toValue);
			stringBuffer.setLength(0);
			//byteBuffer.reset();

			writer.close();
			
			os.flush();
			os.close();
			
			fis.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
		}
		
		
		
		logger.debug("[파일업다운로드] success");
		//return "SUCCESS";
		
		
		JsonObject result_all = result;		//new JsonObject();
		result_all.addProperty("RT_CODE", 1);
		result_all.addProperty("RT_MSG", "SUCCESS");
		//result_all.addProperty("CSV_FILENAME", strFileName);//x?
		//result_all.add("RESULT", result1);
		//response.addHeader("CSV_FILENAME", strFileName);
		//response.setHeader("CSV_FILENAME", strFileName);

		return result_all.toString();
		
	}
	
	
	
}