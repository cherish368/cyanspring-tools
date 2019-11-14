package org.cyanspring.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cyanspring.tools.common.BaseResult;
import org.cyanspring.tools.common.Message;
import org.cyanspring.tools.model.HistoricalBase;
import org.cyanspring.tools.service.HistoricalDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/v1/historicalData")
@Api(value = "/", description = "历史K线处理工具")
public class HistoricalDataController {

    private static final Logger log = LoggerFactory.getLogger(HistoricalDataController.class);

    @Autowired
    private HistoricalDataService historicalDataService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
    @Api(value="用户controller",tags={"用户操作接口"})
    @RestController
    public class UserController {
         @ApiOperation(value="获取用户信息",tags={"获取用户信息copy"},notes="注意问题点")
         @GetMapping("/getUserInfo")
         public User getUserInfo(@ApiParam(name="id",value="用户id",required=true) Long id,@ApiParam(name="username",value="用户名") String username) {
         // userService可忽略，是业务逻辑
         // User user = userService.getUserInfo();
          return null;
      }
    }

    @RequestMapping(value = "/exportByTime", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "导出时间段K线Json")
    public BaseResult<String> exportByTime(String subSymbol, String startTime, String endTime) {

        log.info("HistoricalDataController exportByTime");
        long start = System.currentTimeMillis();
        String result = historicalDataService.exportAllJson(subSymbol, startTime, endTime);
        long end = System.currentTimeMillis();
        log.info((end - start) * 100 * 0.01 / (1000 * 60) + " minutes");
        return new BaseResult<>(Message.OK, result);
    }

    @RequestMapping(value = "/insertOrUpdateByJson", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "插入时间段缺失K线")
    public BaseResult<String> insertOrUpdateByJson() {

        log.info("HistoricalDataController insertOrUpdateByJson");
        long start = System.currentTimeMillis();
        Message result = historicalDataService.insertOrUpdateByJson();
        long end = System.currentTimeMillis();
        log.info((end - start) * 100 * 0.01 / (1000 * 60) + " minutes");
        return new BaseResult<>(result, result.getText());
    }

    @RequestMapping(value = "/updateByValue", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新时间点错误K线")
    public BaseResult<String> updateByValue(String subSymbol, String keyTime, HistoricalBase base) {

        log.info("HistoricalDataController updateByValue");
        long start = System.currentTimeMillis();
        Message result = historicalDataService.updateByValue(subSymbol, keyTime, base);
        long end = System.currentTimeMillis();
        log.info((end - start) * 100 * 0.01 / (1000 * 60) + " minutes");
        return new BaseResult<>(result, result.getText());
    }

}
