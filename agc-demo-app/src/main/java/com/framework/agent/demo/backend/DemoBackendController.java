package com.framework.agent.demo.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HTTP surface for “microservices” the agent tools call. Try with curl while the app runs.
 */
@RestController
@RequestMapping("/demo/backend")
public class DemoBackendController {

    private final DemoToolBackend backend;

    public DemoBackendController(DemoToolBackend backend) {
        this.backend = backend;
    }

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "toolVersion", required = false) String toolVersion
    ) {
        return backend.search(q, toolVersion);
    }

    @GetMapping("/read")
    public Map<String, Object> read(@RequestParam(name = "resourceId", required = false) String resourceId) {
        return backend.readDocument(resourceId);
    }

    @PostMapping("/admin/purge")
    public Map<String, Object> purge() {
        return backend.adminPurge();
    }

    @PostMapping("/payments")
    public Map<String, Object> payments(@RequestBody(required = false) Map<String, Object> body) {
        int cents = 0;
        if (body != null && body.get("amountCents") instanceof Number n) {
            cents = n.intValue();
        }
        return backend.queuePayment(cents);
    }
}
