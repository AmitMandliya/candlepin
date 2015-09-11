# -*- coding: utf-8 -*-
require 'spec_helper'
require 'candlepin_scenarios'
require 'rexml/document'

describe 'Consumer CDK Resource' do

  include CandlepinMethods

  before(:each) do
    @owner = create_owner random_string('test_owner')
    @username = random_string("user")
    @consumername = random_string("cdk_consumer")
    @user = user_client(@owner, @username)
    @cdk_product = create_product("cdk_product",
                                  "CDK Product",
                                  {:attributes => { :expired_after => "60"}})
    @p_product1 = create_product("p_product_1",
                                  "Provided Product 1")
    @p_product2 = create_product("p_product",
                                  "Provided Product 2")
    @consumer = consumer_client(@user, @consumername, :system, 'cdk_user', facts= {:dev_sku => "cdk_product"})
    installed = [
        {'productId' => @cdk_product.id, 'productName' => @cdk_product.name},
        {'productId' => @p_product1.id, 'productName' => @p_product1.name},
        {'productId' => @p_product2.id, 'productName' => @p_product2.name}]
    @consumer.update_consumer({:installedProducts => installed})

    # active subscription to allow this all to work
    active_prod = create_product()
    @active_sub = @cp.create_subscription(@owner['key'], active_prod.id, 10)
    pools = @cp.list_owner_pools(@owner['key'])
    pools.length.should == 1

  end

  it 'should create entitlement to newly created dev pool' do
    pending("candlepin running in standalone mode") if not is_hosted?
    @consumer.consume_product()
    entitlements = @consumer.list_entitlements()
    entitlements.length.should == 1
    new_pool = entitlements[0].pool
    new_pool.type.should == "DEVELOPMENT"
    new_pool.product.id.should == "cdk_product"
    new_pool.providedProducts.length.should == 2
  end
end
