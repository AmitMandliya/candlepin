/**
 * Copyright (c) 2009 - 2018 Red Hat, Inc.
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
package org.candlepin.dto.api.v1;

import org.candlepin.dto.AbstractDTOTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;



/**
 * Test suite for the ImportUpstreamConsumerDTO class
 */
public class ImportUpstreamConsumerDTOTest extends AbstractDTOTest<ImportUpstreamConsumerDTO> {

    protected Map<String, Object> values;

    public ImportUpstreamConsumerDTOTest() {
        super(ImportUpstreamConsumerDTO.class);

        ConsumerTypeDTO type = new ConsumerTypeDTO();
        type.setId("type_id");
        type.setLabel("type_label");
        type.setManifest(true);

        this.values = new HashMap<>();
        this.values.put("Id", "test-id");
        this.values.put("Uuid", "test-uuid");
        this.values.put("Name", "test-name");
        this.values.put("ApiUrl", "test-api-url");
        this.values.put("WebUrl", "test-web-url");
        this.values.put("ConsumerType", type);
        this.values.put("OwnerId", "test-owner-id");
        this.values.put("ContentAccessMode", "test-content-access-mode");
        this.values.put("Created", new Date());
        this.values.put("Updated", new Date());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getInputValueForMutator(String field) {
        return this.values.get(field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getOutputValueForAccessor(String field, Object input) {
        // Nothing to do here
        return input;
    }
}
