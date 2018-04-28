package com.menglin.controller;

import com.menglin.common.ActionResult;

public class BaseController {

    ActionResult<?> createFailActionResult(String errorMessage) {
        ActionResult actionResult = new ActionResult();
        actionResult.fail(500, errorMessage);
        return actionResult;
    }

    ActionResult<Object> createSuccessActionResult(Object data) {
        ActionResult<Object> actionResult = new ActionResult<>();
        actionResult.success(data);
        return actionResult;
    }
}
