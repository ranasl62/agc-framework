package com.framework.agent.architecture;

import com.framework.agent.mcp.DefaultToolInvocationGateway;
import com.framework.agent.mcp.internal.McpToolExecutor;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.framework.agent", importOptions = ImportOption.DoNotIncludeTests.class)
class GovernanceIntegrityArchTest {

    // agc-demo implements McpToolExecutor and may use GatewayContextHolder — exempt via package com.framework.agent.demo..

    private static final DescribedPredicate<JavaCall> MCP_EXECUTOR_EXECUTE =
            new DescribedPredicate<>("call McpToolExecutor.execute") {
                @Override
                public boolean test(JavaCall call) {
                    return "execute".equals(call.getName())
                            && call.getTargetOwner().isAssignableTo(McpToolExecutor.class);
                }
            };

    @ArchTest
    static final ArchRule internalMcpAdapterNotReferencedFromApplicationLayers =
            noClasses()
                    .that()
                    .resideOutsideOfPackages(
                            "com.framework.agent.mcp..",
                            "com.framework.agent.autoconfigure..",
                            "com.framework.agent.demo.."
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.framework.agent.mcp.internal..");

    @ArchTest
    static final ArchRule onlyGatewayInvokesExecutor =
            noClasses()
                    .that()
                    .areNotAssignableFrom(DefaultToolInvocationGateway.class)
                    .and()
                    .resideOutsideOfPackages("com.framework.agent.architecture..")
                    .should()
                    .callMethodWhere(MCP_EXECUTOR_EXECUTE);

    @ArchTest
    static final ArchRule noGatewayScopeSpoofingOutsideMcp =
            noClasses()
                    .that()
                    .resideOutsideOfPackages(
                            "com.framework.agent.mcp..",
                            "com.framework.agent.architecture..",
                            "com.framework.agent.demo.."
                    )
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.framework.agent.mcp.GatewayContextHolder");
}
