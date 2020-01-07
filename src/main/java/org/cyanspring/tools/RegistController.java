package com.xx7x.tzhccb.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mystery.framework.controller.BaseController;
import mystery.framework.model.FileEntity;
import mystery.framework.right.LoginInfoService;
import mystery.framework.utils.StringUtil;
import mystery.framework.utils.UploadUtil;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xx7x.tzhccb.model.common.Result;
import com.xx7x.tzhccb.model.identity.IdentityInfo;
import com.xx7x.tzhccb.service.iface.identity.IIdentityInfoService;
import com.xx7x.tzhccb.support.AppItemConstants;
import com.xx7x.tzhccb.support.AppVoids;
import com.xx7x.usual.support.AppConstants;
import com.xx7x.usual.support.ParameterService;


@Controller
@RequestMapping("/regist")
public class RegistController extends BaseController{
		
	@Autowired
	protected IIdentityInfoService identityService;
	 
	@RequestMapping(value="/regist")
	public String getUser(HttpServletRequest request){
		String InnerAddress=ParameterService.get().getInnerAddress();
		budAttr(request,"InnerAddress",InnerAddress);
		return "regist";
	}	
	
	/**
	 * 上传营业执照
	 * @param request
	 * @param model
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping("/upload")
	public FileEntity upload(HttpServletRequest request,IdentityInfo model) throws FileUploadException, IOException{
		logger.info("---------------start upload------------------------");
		List<FileEntity> entitys = null;
		try {
			UploadUtil uploadUtil = new UploadUtil();
			entitys = uploadUtil.upload(request,true);
			logger.info("----------------upload Success---------------------------");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		logger.info("---------------end upload------------------------------------");		
		return entitys.get(0);
	}

	/**
	 * 企业信息注册
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value="/save")
	@ResponseBody
	public void saveInfo(HttpServletRequest request,HttpServletResponse response,IdentityInfo model){
		
		initJsonStr();
		try{
			boolean flag=identityService.saveInfo(model,request,null);
			jsonStr=(flag==true?successMsg:errorMsg);
		}catch(Exception err){
			jsonStr=errorNullMsg;
			logger.error(err);
		}
		sendJson(response,AppVoids.getJsonNoKey(jsonStr));
    }
	
	/**
	 * 企业信息变更
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value="/change")
	@ResponseBody
	public void change(HttpServletRequest request,HttpServletResponse response,IdentityInfo model){
		
		initJsonStr();
		try{
			loginInfo=getLoginInfo(request);
			boolean flag=identityService.saveInfo(model,request,loginInfo);
			//更新session
			LoginInfoService loginService = new LoginInfoService(request,response);
			loginInfo.setUnitCode(model.getXzqh());
			loginInfo.setLoginName(loginInfo.getLoginName());
			loginInfo.setUserName(model.getQymc());
			loginInfo.setUserId(model.getQybh());
			loginInfo.setUserType(AppItemConstants.TYPE_IDENTITY);			
			//loginInfo.setUserRight(loginInfo.getUserRight());
			loginService.setLoginInfo(loginInfo);					
			loginService.saveSession();			
			jsonStr=(flag==true?successMsg:errorMsg);
		}catch(Exception err){
			jsonStr=errorNullMsg;
			logger.error(err);
		}
		sendJson(response,AppVoids.getJsonNoKey(jsonStr));
    }
	
	
	/**
	 * 企业信息完善
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value="/update")
	@ResponseBody
	public void update(HttpServletRequest request,HttpServletResponse response,IdentityInfo model){	
		initJsonStr();
		try{
			boolean flag=identityService.competeInfo(model);
			jsonStr=(flag==true?successMsg:errorMsg);
		}catch(Exception err){
			jsonStr=errorNullMsg;
			logger.error(err);
		}
		sendJson(response,AppVoids.getJsonNoKey(jsonStr));
    }
	
	@RequestMapping(value="/check")
	@ResponseBody
	public String checkDlmc(HttpServletResponse response,String zzjgdm){
		
		String jsonStr="0";
		try{
			boolean flag=identityService.checkDlmc(zzjgdm);
			jsonStr=(flag==true?"1":"0");
		}catch(Exception err){
			jsonStr=errorNullMsg;
			logger.error(err);
		}
		return jsonStr;
    }	
	

	/**
	 * 根据组织机构代码检测当前企业注册状态
	 * @param request
	 * @param query
	 * @return
	 */
	@RequestMapping(value="/result")
	@ResponseBody
	public Result  registResult(IdentityInfo query){
		Result result=new Result();
		IdentityInfo model=identityService.getInfo(query);
		if(model!=null){
			result.setCode(model.getZt());
			result.setMsg(model.getQybh());
		}else{
			result.setCode(AppConstants.Regist_InExistent);
		}
		return result;		
	}
	
	@RequestMapping(value="/edit")
	public String  registEdit(HttpServletRequest request,IdentityInfo query){
		if(StringUtil.isEmpty(query.getQybh())){
			return "error";
		}
		IdentityInfo model=identityService.getEntity(query);
		budAttr(request,"model",model);
		String InnerAddress=ParameterService.get().getInnerAddress();
		budAttr(request,"InnerAddress",InnerAddress);
		return "regist_edit";
	}
	
	
}