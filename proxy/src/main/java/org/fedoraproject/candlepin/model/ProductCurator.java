/**
 * Copyright (c) 2009 Red Hat, Inc.
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
package org.fedoraproject.candlepin.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;

import com.wideplay.warp.persist.Transactional;

/**
 * interact with Products.
 */
public class ProductCurator extends AbstractHibernateCurator<Product> {

    /**
     * default ctor
     */
    public ProductCurator() {
        super(Product.class);
    }

    /**
     * @param name the product name to lookup
     * @return the Product which matches the given name.
     */
    public Product lookupByName(String name) {
        return (Product) currentSession().createCriteria(Product.class)
            .add(Restrictions.eq("name", name))
            .uniqueResult();
    }

    /**
     * @param id product id to lookup
     * @return the Product which matches the given id.
     */
    public Product lookupById(String id) {
        return (Product) currentSession().createCriteria(Product.class)
            .add(Restrictions.eq("id", id))
            .uniqueResult();
    }
    
    /**
     * Looking a product based on label. This should seldom be used, generally we 
     * refer to products by their ID.
     * 
     * @param label product label to lookup
     * @return the Product which matches the given label.
     */
    public Product lookupByLabel(String label) {
        return (Product) currentSession().createCriteria(Product.class)
            .add(Restrictions.eq("label", label))
            .uniqueResult();
    }
    
    /**
     * @return all Products
     */
    @SuppressWarnings("unchecked")
    public List<Product> listAll() {
        List<Product> results = currentSession().createCriteria(Product.class).list();
        if (results == null) {
            return new LinkedList<Product>();
        }
        else {
            return results;
        }
    }
    
    /**
     * @param updated Product to update
     * @return the updated Product
     */
    @Transactional
    public Product update(Product updated) {
        Product existingProduct = find(updated.getId());
        if (existingProduct == null) {
            return create(updated);
        }
        
        if (updated.getChildProducts() != null) {
            existingProduct.setChildProducts(bulkUpdate(updated.getChildProducts()));
        }
        existingProduct.setLabel(updated.getLabel());
        existingProduct.setName(updated.getName());
        save(existingProduct);
        flush();
        
        return existingProduct;
    }
    
    /**
     * @param products set of products to update.
     * @return updated products.
     */
    @Transactional
    public Set<Product> bulkUpdate(Set<Product> products) {
        Set<Product> toReturn = new HashSet<Product>();
        for (Product toUpdate : products) {
            toReturn.add(update(toUpdate));
        }
        return toReturn;
    }

}
