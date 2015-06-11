/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.pinsetter.tasks;

import org.candlepin.common.config.Configuration;
import org.candlepin.config.ConfigProperties;
import org.candlepin.model.ContentCurator;
import org.candlepin.model.PoolCurator;
import org.candlepin.model.Product;
import org.candlepin.model.ProductContent;
import org.candlepin.model.ProductCurator;
import org.candlepin.service.ProductServiceAdapter;
import org.candlepin.util.Util;

import com.google.inject.Inject;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;



/**
 * The PopulatedHostedDBTask is the asynchronous worker implementation for populating Hosted's DB.
 *
 * This class will likely be removed once the multiorg migration is complete.
 */
public class PopulateHostedDBTask extends KingpinJob {
    private static Logger log = LoggerFactory.getLogger(PopulateHostedDBTask.class);

    private ProductServiceAdapter productService;
    private ProductCurator productCurator;
    private ContentCurator contentCurator;
    private PoolCurator poolCurator;
    private Configuration config;


    @Inject
    public PopulateHostedDBTask(ProductServiceAdapter productService, ProductCurator productCurator,
        ContentCurator contentCurator, PoolCurator poolCurator, Configuration config) {

        this.productService = productService;
        this.productCurator = productCurator;
        this.contentCurator = contentCurator;
        this.poolCurator = poolCurator;
        this.config = config;
    }

    @Override
    public void toExecute(JobExecutionContext context) throws JobExecutionException {
        if (this.config.getBoolean(ConfigProperties.STANDALONE)) {
            log.warn("Aborting populate DB task in standalone environment");
            context.setResult("Aborting populate DB task in standalone environment");
            return;
        }

        int pcount = 0;
        int ccount = 0;
        log.info("Populating Hosted DB");

        Set<String> productCache = new HashSet<String>();
        Set<String> productIds = this.poolCurator.getAllKnownProductIds();
        log.info("Importing data for known products...");

        do {
            Set<String> dependentProducts = new HashSet<String>();

            for (Product product : this.productService.getProductsByIds(productIds)) {
                log.info("Storing product: {}", product);

                dependentProducts.addAll(product.getDependentProductIds());

                for (ProductContent pcontent : product.getProductContent()) {
                    log.info("  Storing product content: {}", pcontent.getContent());
                    this.contentCurator.createOrUpdate(pcontent.getContent());
                    ++ccount;
                }

                this.productCurator.createOrUpdate(product);
                ++pcount;
            }

            log.info("Importing data for dependent products...");
            productCache.addAll(productIds);
            dependentProducts.removeAll(productCache);
            productIds = dependentProducts;
        } while (productIds.size() > 0);

        // TODO: Should this be translated?
        String result = String.format(
            "Finished populating Hosted DB. Received %d product(s) and %d content",
            pcount, ccount
        );

        log.info(result);
        context.setResult(result);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    public static JobDetail createAsyncTask() {
        JobDetail detail = JobBuilder.newJob(PopulateHostedDBTask.class)
            .withIdentity("populated_hosted_db-" + Util.generateUUID())
            .requestRecovery(true)
            .build();

        return detail;
    }

}
