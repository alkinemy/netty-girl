package al.netty.apiserver.domain.server.service;

import al.netty.apiserver.domain.server.exception.RequestParamException;
import al.netty.apiserver.domain.server.exception.ServiceException;
import com.google.gson.JsonObject;

public interface ApiRequest {
    void requestParamValidation() throws RequestParamException;

    void service() throws ServiceException;

    void executeService();

    JsonObject getApiResult();
}
