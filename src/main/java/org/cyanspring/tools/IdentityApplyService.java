package com.xx7x.tzhccb.service.impl.identity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mystery.framework.model.LoginInfo;
import mystery.framework.model.page.Page;
import mystery.framework.model.page.QueryParamers;
import mystery.framework.utils.DateUtil;
import mystery.framework.utils.FileUtil;
import mystery.framework.utils.ListUtil;
import mystery.framework.utils.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xx7x.tzhccb.mapper.identity.IdentityApplyMapper;
import com.xx7x.tzhccb.mapper.identity.IdentityProjectCityMapper;
import com.xx7x.tzhccb.mapper.identity.IdentityProjectCountyMapper;
import com.xx7x.tzhccb.mapper.identity.IdentityProjectMapper;
import com.xx7x.tzhccb.model.excfg.ExCfgColumn;
import com.xx7x.tzhccb.model.identity.IdentityApply;
import com.xx7x.tzhccb.model.identity.IdentityInfo;
import com.xx7x.tzhccb.model.identity.IdentityProject;
import com.xx7x.tzhccb.model.identity.IdentityProjectCity;
import com.xx7x.tzhccb.model.identity.IdentityProjectCounty;
import com.xx7x.tzhccb.model.image.ImageFunction;
import com.xx7x.tzhccb.model.project.ProjectPrice;
import com.xx7x.tzhccb.service.iface.identity.IIdentityApplyService;
import com.xx7x.tzhccb.service.iface.identity.IIdentityInfoService;
import com.xx7x.tzhccb.service.iface.image.IImageInfoService;
import com.xx7x.tzhccb.service.iface.project.IProjectPriceService;
import com.xx7x.tzhccb.support.AppItemConstants;
import com.xx7x.tzhccb.support.AppVoids;
import com.xx7x.tzhccb.support.service.WordExpService;
import com.xx7x.usual.mapper.code.CodeNodeInfoMapper;
import com.xx7x.usual.service.iface.code.ICodeNodeInfoService;
import com.xx7x.usual.service.impl.BaseItemService;
import com.xx7x.usual.support.AppConstants;
import com.xx7x.usual.support.CacheVoids;


@Service("identityApplyService")
public class IdentityApplyService extends BaseItemService implements IIdentityApplyService {

	@Autowired
	protected IdentityApplyMapper identityApplyMapper;	
	@Autowired
	protected IdentityProjectMapper identityProjectMapper;
	@Autowired
	protected IdentityProjectCountyMapper identityProjectCountyMapper;
	@Autowired
	protected IdentityProjectCityMapper identityProjectCityMapper;
	@Autowired
	protected CodeNodeInfoMapper codeNodeInfoMapper;
	@Autowired
	protected IIdentityInfoService identityInfoService;
	@Autowired
	protected ICodeNodeInfoService codeNodeInfoService;
	@Autowired
	protected IProjectPriceService projectPriceService;
	@Autowired
	protected IImageInfoService imageInfoService;
	
	
	/**
	 * 企业申报信息保存
	 */
	@Transactional("transactionManager_default")
	public  String saveInfo(String[] images,IdentityApply model,IdentityProject project,LoginInfo loginInfo) {
		//通过节点更新节点名称和状态信息
		model.fillNode(model.getDqjd(),loginInfo);	
		String key=model.getSbbh();
		if(StringUtil.isEmpty(key)){
			//保存申报信息(一对一关系，保持三表主键一致)
			String prefix=model.getXzqh()==null?"331000000":model.getXzqh();
			key = getMaxId("SBBH",prefix,9,5,identityApplyMapper);
			model.setSbbh(key);	
			model.init();
			model.setTbbj(AppConstants.TBBJ_DEFAULT);
			insert(identityApplyMapper,model);	
			//保存项目信息
			if(project!=null){				
				project.setXmbh(key);
				project.init();
				insert(identityProjectMapper, project);					
				//保存指标信息
				if("1".equals(model.getXmlb())){
					IdentityProjectCounty county=project.getCounty();
					if(county==null){
						county=new IdentityProjectCounty();
					}	
					county.setZbbh(key);
					county.init();
					insert(identityProjectCountyMapper,county);	
				}else if("2".equals(model.getXmlb())){
					IdentityProjectCity city=project.getCity();
					if(city==null){
						city=new IdentityProjectCity();	
					}
					city.setZbbh(key);
					city.init();
					insert(identityProjectCityMapper,city);
				}					
			}
		}else{
			//更新申报信息						
			update(identityApplyMapper, model);	
			if(project!=null&&StringUtil.isNotEmpty(project.getXmbh())){				
				//更新项目信息		
				update(identityProjectMapper, project);					
				//更新指标信息
				if("1".equals(model.getXmlb())){
					IdentityProjectCounty county=project.getCounty();
					if(county!=null&&StringUtil.isNotEmpty(county.getZbbh())){
						update(identityProjectCountyMapper,county);
						//只要县级通过就进行资金结算
						if(AppItemConstants.NODE_CALCU.contains(model.getDqjd())){
							projectPriceService.saveCounty(model,county,loginInfo,project);
						}
					}
						
				}else if("2".equals(model.getXmlb())){
					IdentityProjectCity city=project.getCity();
					if(city!=null&&StringUtil.isNotEmpty(city.getZbbh())){
						update(identityProjectCityMapper,city);	
						//只要县级通过就进行资金结算
						if(AppItemConstants.NODE_CALCU.contains(model.getDqjd())){
							projectPriceService.saveCity(model,city,loginInfo,project);
						}
					}
				}				
			}			
		}
		//保存影像信息
		imageInfoService.saveInfo(model,images);		
		//保存节点流水信息
		if(StringUtil.isNotEmpty(model.getSgjd())&&!model.getDqjd().equals(model.getSgjd())){
			codeNodeInfoService.save(model,loginInfo);
		}
		return key;		
	}
	
	
	/**
	 * 待办事项管理查询
	 * 企业申报列表信息查询
	 */
	public void getPage(LoginInfo loginInfo,Page<IdentityApply> mypage,IdentityApply query) {
		addPageParamers(mypage);
		String filter=" 1=1";
		String userType=loginInfo.getUserType();
		if(StringUtil.isNotEmpty(userType)){
			if("1".equals(userType)){  //企业用户
				filter+=" and a.QYBH = '"+loginInfo.getUserId()+"'";
			}else{ 					   //环保用户
				if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
					String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
					filter+=" and a.XZQH like '"+Xzqh+"%'";
				}
			}
		}
		String jsbh=loginInfo.getUserRight();
		String code=CacheVoids.getSysNodeRole(jsbh);
		if(StringUtil.isNotEmpty(code)){
			filter+=" and a.DQJD  in("+code+")";
		}
		if(StringUtil.isNotEmpty(query.getXzqh())){
			filter+=" and a.XZQH  in("+StringUtil.formatByTag(query.getXzqh())+")";
		}
		if(StringUtil.isNotEmpty(query.getTjzt())){
			filter+=" and a.TJZT ='"+query.getTjzt()+"'";
		}
		if(StringUtil.isNotEmpty(query.getQymc())){
			filter+=" and a.QYMC like '%"+query.getQymc()+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXmmc())){
			filter+=" and b.Xmmc like '%"+query.getXmmc()+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXmlb())){
			filter+=" and b.Xmlb ='"+query.getXmlb()+"'";
		}
		mypage.setSort(" a.SJC desc");
		mypage.addQueryFilter("queryStr",filter);
		List<IdentityApply> list=identityApplyMapper.getleftPage(mypage.gotQueryFilter());	
		long total=identityApplyMapper.getleftCount(mypage.gotQueryFilter());
		mypage.setRows(list);
		mypage.setTotal(total);
		
	}
	
	/**
	 * 在办事项管理查询
	 * 企业申报列表信息查询
	 */
	public void getQueryPage(LoginInfo loginInfo,Page<IdentityApply> mypage,IdentityApply query) {
		addPageParamers(mypage);
		String filter=" a.dqjd <> '1006' ";
		String userType=loginInfo.getUserType();
		if(StringUtil.isNotEmpty(userType)){
			if("1".equals(userType)){  //企业用户
				filter+=" and a.QYBH = '"+loginInfo.getUserId()+"'";
			}else{ 					   //环保用户
				if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
					String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
					filter+=" and a.XZQH like '"+Xzqh+"%'";
				}
			}
		}
		if(StringUtil.isNotEmpty(query.getXzqh())){
			filter+=" and a.XZQH  in("+StringUtil.formatByTag(query.getXzqh())+")";
		}
		if(StringUtil.isNotEmpty(query.getQymc())){
			filter+=" and a.QYMC like '%"+query.getQymc()+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXmlb())){
			filter+=" and b.Xmlb ='"+query.getXmlb()+"'";
		}
		mypage.setSort(" a.SJC desc");
		mypage.addQueryFilter("queryStr",filter);
		List<IdentityApply> list=identityApplyMapper.getleftPage(mypage.gotQueryFilter());	
		long total=identityApplyMapper.getleftCount(mypage.gotQueryFilter());
		mypage.setRows(list);
		mypage.setTotal(total);						
	}
	
	
	/**
	 * 企业历史申报列表信息查询
	 */
	public void getHistoryPage(LoginInfo loginInfo,Page<IdentityApply> mypage,IdentityApply query) {
		addPageParamers(mypage);
		String filter=" a.dqjd='1006'";
		String userType=loginInfo.getUserType();
		if(StringUtil.isNotEmpty(userType)){
			if("1".equals(userType)){  //企业用户
				filter+=" and a.QYBH = '"+loginInfo.getUserId()+"'";
			}else{ 					   //环保用户
				if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
					String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
					filter+=" and a.XZQH like '"+Xzqh+"%'";
				}
			}
		}
		if(StringUtil.isNotEmpty(query.getXzqh())){
			filter+=" and a.XZQH  in("+StringUtil.formatByTag(query.getXzqh())+")";
		}
		if(StringUtil.isNotEmpty(query.getQymc())){
			filter+=" and a.QYMC like '%"+query.getQymc()+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXmlb())){
			filter+=" and b.Xmlb ='"+query.getXmlb()+"'";
		}
		mypage.setSort(" a.SJC desc");
		mypage.addQueryFilter("queryStr",filter);
		List<IdentityApply> list=identityApplyMapper.getleftPage(mypage.gotQueryFilter());	
		long total=identityApplyMapper.getleftCount(mypage.gotQueryFilter());
		mypage.setRows(list);
		mypage.setTotal(total);						
	}



	/**
	 * 从登录信息获取一些企业信息
	 * 节点信息
	 * @param loginInfo
	 * @return
	 */
	@Transactional("transactionManager_default")
	public IdentityApply getEntityInfo(LoginInfo loginInfo,IdentityApply apply,boolean ischeck){
		if(StringUtil.isEmpty(apply.getSbbh())){			
			if(loginInfo!=null){
				//IdentityInfo query=new IdentityInfo();
				//query.setQybh(loginInfo.getUserId());
				//IdentityInfo model=identityInfoService.getEntity(query);
				//填充企业基本信息
				if(StringUtil.isNotEmpty(loginInfo.getUserId())){
					IdentityInfo info=new IdentityInfo();
					info.setQybh(loginInfo.getUserId());
					IdentityInfo model=identityInfoService.getEntity(info);
					apply.fillBase(model);
					if(model!=null){
						apply.setQybh(model.getQybh());
						apply.setQymc(model.getQymc());
						apply.setDwmc(model.getQymc());
						apply.setQydz(model.getQydz());
						apply.setXzqh(model.getXzqh());
						
					}
				}				
				apply.fillNodes(null, AppItemConstants.CODE_NODE_KINDID);			
			}
			//初始化上传图片的框
			List<ImageFunction> images=imageInfoService.fillImages(AppItemConstants.APPLY_GNBH);
			apply.setImages(images);
			return apply;
		}else{
			IdentityApply model=identityApplyMapper.getleftEntity(apply);			
			if("1".equals(model.getXmlb())){
				IdentityProjectCounty obj=new IdentityProjectCounty();
				obj.setZbbh(apply.getSbbh());
				IdentityProjectCounty county=identityProjectCountyMapper.getEntity(obj);
				model.setCounty(county);
			}else if("2".equals(model.getXmlb())){
				IdentityProjectCity obj=new IdentityProjectCity();
				obj.setZbbh(apply.getSbbh());
				IdentityProjectCity city=identityProjectCityMapper.getEntity(obj);
				model.setCity(city);
			}
			model.fillNodes(model.getDqjd(), AppItemConstants.CODE_NODE_KINDID);
			//查询图片和附件信息
			List<ImageFunction> images=imageInfoService.getList(model.getSbbh(),AppItemConstants.APPLY_GNBH);
			model.setImages(images);
			/**
			String jsbh=loginInfo.getUserRight();
			if(AppConstants.SYS_ROLE_CITY.equals(jsbh)&&ischeck){
				//市级环保待办案件进行分配（未分配的）
				if(StringUtil.isEmpty(model.getSprbh())){
					IdentityApply query=new IdentityApply();
					query.setSbbh(model.getSbbh());
					query.setSprbh(loginInfo.getUserId());
					update(identityApplyMapper,query);
					model.setSprbh(loginInfo.getUserId());
				}
			}
			*/
			return model;
		}	
	}
	

	/**
	 * 导出文档信息
	 * 多个文档打包下载
	 */
	public void printWord(HttpServletResponse response,HttpServletRequest request, IdentityApply query,String Gnbh) {
		if(StringUtil.isNotEmpty(query.getSbbh())){
			//获取申报信息
			IdentityApply apply=new  IdentityApply();
			apply.setSbbh(query.getSbbh());
			IdentityApply model=identityApplyMapper.getleftEntity(apply);	
			if("1-sb".equals(query.getXzll())){
				if("1".equals(model.getXmlb())){
					IdentityProjectCounty obj=new IdentityProjectCounty();
					obj.setZbbh(apply.getSbbh());
					IdentityProjectCounty county=identityProjectCountyMapper.getEntity(obj);
					model.setCounty(county);
				}else if("2".equals(model.getXmlb())){
					IdentityProjectCity obj=new IdentityProjectCity();
					obj.setZbbh(apply.getSbbh());
					IdentityProjectCity city=identityProjectCityMapper.getEntity(obj);
					model.setCity(city);
				}
			}else if("2-sg".equals(query.getXzll())){				
				ProjectPrice price=projectPriceService.getEntity(apply.getSbbh());
				model.setPrice(price);
			}			
			model.setXzll(query.getXzll());
			if(model!=null){			
				if(StringUtil.isNotEmpty(query.getXzll())){					
					model.fillModel();				
				}																
				WordExpService downRarService = new WordExpService();
				String downFilePathName = downRarService.downService(model);				
				if(StringUtil.isNotEmpty(downFilePathName)) {
					
					String localFileName = StringUtil.substring(downFilePathName,downFilePathName.lastIndexOf("_"));
					byte[] data = downRarService.getBytes(downFilePathName);
					try {
						pullSteam(model,response,data,localFileName,downFilePathName);
						FileUtil.deleteFile(downFilePathName);	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
															    
			}			
		}
	}
	
	/**
	 * 下载文件
	 * @param response
	 * @param data
	 * @param fileName
	 * @param remoteFileName
	 * @throws IOException
	 */
	protected void pullSteam(IdentityApply query,HttpServletResponse response,byte[] data,String fileName,String remoteFileName) throws IOException{
		try{
			if(data!=null){
				if(!StringUtil.isEmpty(fileName)){	
					
					query.setXzll(query.getXzll()+query.getXmlb());					
					String name=CacheVoids.getCodeInfoName(CacheVoids.getDcmbs(),query.getXzll());
					String pullName=name+"_"+DateUtil.getYMHS();
					fileName=new String(pullName.getBytes("UTF-8"), "ISO8859-1"); 
					response.setHeader("Content-Disposition", "attachment; filename=" +fileName+".doc");  
				}
		        OutputStream outStream = response.getOutputStream();
		        outStream.write(data);
		        outStream.flush();
		        outStream.close();
		        response.flushBuffer();
				if(!StringUtil.isEmpty(remoteFileName))
					FileUtil.deleteFile(remoteFileName);
			}
		}catch(Exception err){
			logger.error(err);
		}
	}


	/**
	 * 申报信息删除
	 * 
	 */
	@Transactional("transactionManager_default")
	public boolean delete(String key) {
		if(StringUtil.isNotEmpty(key)){
			IdentityApply apply=new  IdentityApply();
			apply.setSbbh(key);
			IdentityApply model=identityApplyMapper.getleftEntity(apply);
			if(model!=null){
				//主信息删除
				delete(identityApplyMapper,apply);
				//项目信息删除
				IdentityProject project=new IdentityProject();
				project.setXmbh(key);
				delete(identityProjectMapper, project);
				//子信息删除
				if("1".equals(model.getXmlb())){
					//县批项目子信息
					IdentityProjectCounty count=new IdentityProjectCounty();
					count.setZbbh(key);
					delete(identityProjectCountyMapper, count);
				}else if("2".equals(model.getXmlb())){
					//市批项目子信息
					IdentityProjectCity city=new IdentityProjectCity();
					city.setZbbh(key);
					delete(identityProjectCityMapper, city);
				}
				//删除影像信息
				imageInfoService.delete(key, AppItemConstants.APPLY_GNBH);
				//删除审批日志
				codeNodeInfoService.delete(key);
				//删除合算信息
				projectPriceService.delete(key);
			}
			return true;
		}
		return false;
	}
	   
	/**
	 * 查询审核审核日志
	 * @param loginInfo
	 * @param mypage
	 * @param query
	 */
	public void getLogPage(LoginInfo loginInfo,Page<IdentityApply> mypage,IdentityApply query){
		addPageParamers(mypage);
		String filter=" c.type<>'330000005'";
		if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
			String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
			filter+=" and i.XZQH like '"+Xzqh+"%'";
		}					
		if(StringUtil.isNotEmpty(loginInfo.getUserId())){	
			if("1".equals(query.getIsme()))
				filter+=" and c.OPT_USER_IDS = '"+loginInfo.getUserId()+"'";
			else if("0".equals(query.getIsme()))
				filter+=" and c.OPT_USER_IDS <> '"+loginInfo.getUserId()+"'";
		}
		if(StringUtil.isNotEmpty(query.getQymc())){			
			filter+=" and i.QYMC like '%"+query.getQymc()+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXzqh())){			
			filter+=" and i.XZQH in ("+StringUtil.formatByTag(query.getXzqh())+")";
		}
		mypage.addQueryFilter("queryStr", filter);
		List<IdentityApply> list= codeNodeInfoMapper.getleftPage(mypage.gotQueryFilter());
		long total=codeNodeInfoMapper.getleftCount(mypage.gotQueryFilter());
		mypage.setRows(list);
		mypage.setTotal(total);
	}


	/**
	 * 申报信息导出
	 * @param info
	 * @param request
	 * @param response
	 * @param query
	 */
    public void export(String[] chks,LoginInfo loginInfo,HttpServletRequest request,
    		HttpServletResponse response,IdentityApply query){    	   	    	
        QueryParamers queryParamers=new QueryParamers();
		String filter=" a.dqjd='1006'";
	 	//环保用户
		if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
			String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
			filter+=" and a.XZQH like '"+Xzqh+"%'";
		}
		if(StringUtil.isNotEmpty(query.getXzqh())){
			filter+=" and a.XZQH  in("+StringUtil.formatByTag(query.getXzqh())+")";
		}
		if(StringUtil.isNotEmpty(query.getQymc())){
			filter+=" and a.QYMC like '%"+query.getQymc()+"%'";
		}
        queryParamers.addQueryFilter("queryStr", filter);
    	List<IdentityApply> collect=identityApplyMapper.getleftList(queryParamers.getQueryFilter());
    	if(!ListUtil.isEmpty(collect)){
    		for(IdentityApply i:collect){
    			if(StringUtil.isNotEmpty(i.getXzqh())){
    				i.setXzqh(CacheVoids.getSysEnvUnitValue(i.getXzqh()));
    			}
    			if(StringUtil.isNotEmpty(i.getTjzt())){
    				i.setTjzt(CacheVoids.getCodeInfoValue(CacheVoids.getSbzts(), i.getTjzt()));   				
    			}
    			if(StringUtil.isNotEmpty(i.getXmlb())){
    				i.setXmlb(CacheVoids.getCodeInfoValue(CacheVoids.getXmlbs(), i.getXmlb()));   				
    			}
    			i.fillModel();
    		}
    	}
    	
		List<String> list=new ArrayList<String>();
        list.add(AppItemConstants.EXPORT_TABLEID1); 
   
        Map<String,Object> data=new HashMap<String,Object>();
        data.put(AppItemConstants.EXPORT_TABLEID1, collect);
      
         List<ExCfgColumn> arr=new ArrayList<ExCfgColumn>();
        if(chks!=null&&chks.length>0){      	
        	for(int i=0;i<chks.length;i++){
        		ExCfgColumn col=CacheVoids.getExCfgColumn(chks[i],AppItemConstants.EXPORT_TABLEID1);
        		arr.add(col);
        	}
        }
        if(!ListUtil.isEmpty(arr))
        	data.put(AppItemConstants.EXPORT_TABLEID1+"col",arr); 
      
        export(list,request,response,data); 
        
	}
    
    /**
     * 查询前三个待办事项（首页）
     * @param loginInfo
     * @return
     */
    public List<IdentityApply> getNewList(LoginInfo loginInfo){
    	QueryParamers queryParamers=new QueryParamers();
    	String filter=" 1=1";
    	String userType=loginInfo.getUserId();
    	if(StringUtil.isNotEmpty(userType)){
			if("1".equals(userType)){  //企业用户
				filter+=" and a.QYBH = '"+loginInfo.getUserId()+"'";
			}else{ 					   //环保用户
				if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
					String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
					filter+=" and a.XZQH like '"+Xzqh+"%'";
				}
			}
		}	
    	String jsbh=loginInfo.getUserRight();
		String code=CacheVoids.getSysNodeRole(jsbh);
		if(StringUtil.isNotEmpty(code)){
			filter+=" and a.DQJD  in("+code+")";
		}
		filter+=" order by a.sjc desc limit 3";
    	queryParamers.addQueryFilter("queryStr", filter);
    	List<IdentityApply> list=identityApplyMapper.getleftList(queryParamers.getQueryFilter());
    	return list;
    }
    
    /**
     * 操作日志（首页）
     * @param loginInfo
     * @return
     */
    public List<IdentityApply> getLogList(LoginInfo loginInfo){
    	QueryParamers queryParamers=new QueryParamers();
		String filter=" 1=1";			
		String userType=loginInfo.getUserId();
		if(StringUtil.isNotEmpty(userType)){
			if("1".equals(userType)){  //企业用户
				filter+=" and c.OPT_USER_IDS = '"+loginInfo.getUserId()+"'";
			}else{ 					   //环保用户
				if(StringUtil.isNotEmpty(loginInfo.getUnitCode())){
					String Xzqh=AppVoids.getEnvUnitCodeHead(loginInfo.getUnitCode());
					filter+=" and c.type<>'330000005' and i.XZQH like '"+Xzqh+"%'";
				}
			}
		}		
		queryParamers.addQueryFilter("queryStr", filter);
		List<IdentityApply> list= codeNodeInfoMapper.getleftList(queryParamers.getQueryFilter());
		return list;
    }
    
     
}