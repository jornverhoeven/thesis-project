package tech.jorn.adrian.risks;

import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.risks.rules.*;
import tech.jorn.adrian.risks.rules.cves.EntryCve;
import tech.jorn.adrian.risks.rules.cves.FirmwareVulnerabilityCve;
import tech.jorn.adrian.risks.rules.cves.OsVulnerabilityCve;
import tech.jorn.adrian.risks.rules.cves.SdkVulnerabilityCve;
import tech.jorn.adrian.risks.validators.AnyVersion;
import tech.jorn.adrian.risks.validators.BeforeVersion;

import java.util.List;

public class RiskLoader {
    public static List<RiskRule> listRisks() {
        return List.of(
                new SdkVulnerabilityCve("CVE-2021-22547", "google/cloud_iot_device_sdk_for_embedded_c", new BeforeVersion("1.0.3"), 1.8f, "1.0.3"), // https://nvd.nist.gov/vuln/detail/CVE-2021-22547
                new FirmwareVulnerabilityCve("CVE-2022-25666", "qualcomm/apq8096", new AnyVersion(), 0.8f), // https://nvd.nist.gov/vuln/detail/CVE-2022-25666
                new FirmwareVulnerabilityCve("CVE-2020-3676", "qualcomm/apq8096", new AnyVersion(), 1.8f)  // https://nvd.nist.gov/vuln/detail/CVE-2020-3676
                        .fromSoftwareToNode(),
                new OsVulnerabilityCve("CVE-2022-35927", "contiki-ng/contiki-ng", new BeforeVersion("4.7"), 3.9f, "4.7"), // https://nvd.nist.gov/vuln/detail/CVE-2022-35927

                new EntryCve("CVE-2022-35927", "os-contiki-ng/contiki-ng-version", new BeforeVersion("4.7"), 3.9f, "4.7")
                        .includeNodes(),
                new EntryCve("CVE-2021-40830", "sdk-amazon/amazon_web_services_iot_device_sdk_v2-version", new BeforeVersion("1.5"), 2.8f, "1.5")
                        .includeSoftware()

//                new RuleInfrastructureNodeHasFirewall(),
//                new RuleInfrastructureNodeIsPhysicallySecured(),
//                new RuleSoftwareComponentIsEncrypted()
        );
    }
}
