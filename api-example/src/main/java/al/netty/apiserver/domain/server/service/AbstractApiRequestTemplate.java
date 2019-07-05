package al.netty.apiserver.domain.server.service;

import al.netty.apiserver.domain.server.exception.RequestParamException;
import al.netty.apiserver.domain.server.exception.ServiceException;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public abstract class AbstractApiRequestTemplate implements ApiRequest {

    protected Logger logger;

    protected Map<String, String> requestData;

    protected JsonObject apiResult;

    public AbstractApiRequestTemplate(Map<String, String> requestData) {
        this.logger = LogManager.getLogger(this.getClass());
        this.apiResult = new JsonObject();
        this.requestData = requestData;

        logger.info("request data: " + this.requestData);
    }

    @Override
    public void executeService() {
        try {
            this.requestParamValidation();
            this.service();
        } catch (RequestParamException e) {
            logger.error(e);
            this.apiResult.addProperty("resultCode", "405");
        } catch (ServiceException e) {
            logger.error(e);
            this.apiResult.addProperty("resultCode", "501");
        }
    }

    @Override
    public JsonObject getApiResult() {
        return this.apiResult;
    }

}
