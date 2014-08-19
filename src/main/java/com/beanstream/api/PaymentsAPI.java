/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Beanstream Internet Commerce Corp, Digital River, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.beanstream.api;

import com.beanstream.Configuration;
import com.beanstream.connection.BeanstreamUrls;
import com.beanstream.connection.HttpMethod;
import com.beanstream.connection.HttpsConnector;
import com.beanstream.exceptions.BeanstreamApiException;
import com.beanstream.requests.CardPaymentRequest;
import com.beanstream.responses.PaymentResponse;
import com.google.gson.Gson;
import java.text.MessageFormat;

/**
 * The entry point for processing payments.
 * 
 * @author bowens
 */
public class PaymentsAPI {
    
    private Configuration config;
    
    public PaymentsAPI(Configuration config) {
        this.config = config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    
    public PaymentResponse makePayment(CardPaymentRequest paymentRequest) throws BeanstreamApiException {
        paymentRequest.setMerchant_id( ""+config.getMerchantId() );
        paymentRequest.getCard().setComplete( true ); // false for pre-auth
        
        HttpsConnector connector = new HttpsConnector(config.getMerchantId(), config.getApiPasscode());
        
        // build the URL
        String url = MessageFormat.format(BeanstreamUrls.BasePaymentsUrl, config.getPlatform(), config.getVersion());
        
        // process the transaction using the REST API
        String response = connector.ProcessTransaction(HttpMethod.post, url, paymentRequest);
        
        // parse the output and return a PaymentResponse
        Gson gson = new Gson();
        return gson.fromJson(response, PaymentResponse.class);
    }
}
