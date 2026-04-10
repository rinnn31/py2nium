package io.py2nium.server.handler.apps;

import static io.py2nium.server.utils.ModelConverter.toModelAndValidate;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.api.InstallModel;

public class InstallAppCommand extends CommandHandler {
    public InstallAppCommand(String uri, HttpMethod... methods) {
        super(uri, methods);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        InstallModel model = toModelAndValidate(request.getBody(), InstallModel.class);
        try {
            if(model.isXApk()) {
                io.py2nium.server.utils.ApkInstaller.installXApk(model);
            } else {
                io.py2nium.server.utils.ApkInstaller.installApk(model);
            }
            return new Py2niumResponse(Py2niumResponse.SUCCESS, null, null);
        } catch (Exception e) {
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }
    }
}
