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

import java.util.List;
import java.util.Optional;

import com.beanstream.Configuration;
import com.beanstream.Gateway;
import com.beanstream.connection.BeanstreamUrls;
import com.beanstream.connection.HttpMethod;
import com.beanstream.connection.HttpsConnector;
import com.beanstream.domain.Address;
import com.beanstream.domain.Card;
import com.beanstream.domain.CustomFields;
import com.beanstream.domain.PaymentProfile;
import com.beanstream.domain.Token;
import com.beanstream.exceptions.BeanstreamApiException;
import com.beanstream.requests.ProfileRequest;
import com.beanstream.responses.ProfileCardsResponse;
import com.beanstream.responses.ProfileResponse;
import com.beanstream.util.ProfilesUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Payment Profiles allow you to store a customer's card number and other
 * information, such as billing address and shipping address. The card number
 * stored on the profile is a multi-use token and is called the ID.
 * 
 * Profiles can be created with a Credit Card or with a single-use Legato token.
 * If using a token then the card information needs to be entered each time the
 * user checks out. However the profile will always save the customer's billing
 * info.
 * 
 * @author Pedro Garcia
 *
 */
public class ProfilesAPI {

	private Configuration config;
	private HttpsConnector connector;
	private final Gson gson = new Gson();

	public ProfilesAPI(Configuration config) {
		this.config = config;
		connector = new HttpsConnector(config.getMerchantId(),
				config.getProfilesApiPasscode());
	}

	public void setConfig(Configuration config) {
		this.config = config;
		connector = new HttpsConnector(config.getMerchantId(),
				config.getProfilesApiPasscode());
	}

	/**
	 * Create a PaymentProfile using a card and a billing address
	 * 
	 * @param card
	 *            mandatory card, must be a valid card
	 * @param billing
	 *            address of the credit card
	 * @return a ProfileResonse saying if the profile was created or not and the
	 *         message
	 * @throws BeanstreamApiException
	 *             if any validation fails or error occur
	 */
	public ProfileResponse createProfile(Card card, Address billing)
			throws BeanstreamApiException {
		return createProfile(card, null, billing, null, null, null);
	}

	/**
	 * Create a PaymentProfile using a token, billing address
	 * 
	 * @param token
	 *            mandatory token, must contain a valid name and code
	 * @param billing
	 *            address of the credit card
	 * @return a ProfileResonse saying if the profile was created or not and the
	 *         message
	 * @throws BeanstreamApiException
	 *             if any validation fails or error occur
	 */
	public ProfileResponse createProfile(Token token, Address billing)
			throws BeanstreamApiException {
		return createProfile(null, token, billing, null, null, null);
	}

	/**
	 * Create a PaymentProfile using a Card, billing address, custom fields,
	 * language and comments
	 * 
	 * @param card
	 *            mandatory parameter, must contain a valid card
	 * @param billing
	 *            address of the credit card
	 * @param custom
	 *            fields to add in the profile
	 * @param language
	 *            profile language
	 * @param comments
	 * @return a ProfileResonse saying if the profile was created or not and the
	 *         message
	 * @throws BeanstreamApiException
	 *             if any validation fails or error occur
	 */
	public ProfileResponse createProfile(Card card, Address billing,
			CustomFields custom, String language, String comments)
			throws BeanstreamApiException {
		return createProfile(card, null, billing, custom, language, comments);
	}

	/**
	 * Create a PaymentProfile using a token, billing address, custom fields,
	 * language and comments
	 * 
	 * @param token
	 *            mandatory token, must contain a valid name and code
	 * @param billing
	 *            address of the credit card
	 * @param custom
	 *            fields to add in the profile
	 * @param language
	 *            profile language
	 * @param comments
	 * @return a ProfileResonse saying if the profile was created or not and the
	 *         message
	 * @throws BeanstreamApiException
	 *             if any validation fails or error occur
	 */
	public ProfileResponse createProfile(Token token, Address billing,
			CustomFields custom, String language, String comments)
			throws BeanstreamApiException {
		return createProfile(null, token, billing, custom, language, comments);
	}

	/**
	 * Create a PaymentProfile using a token or token, billing address, custom
	 * fields, language and comments you must send either one a token or a card
	 * 
	 * @param token
	 *            mandatory parameter if card object is null, must contain a
	 *            valid name and code
	 * @param card
	 *            mandatory parameter if token object is null, must contain a
	 *            valid card
	 * @param billing
	 *            address of the credit card
	 * @param custom
	 *            fields to add in the profile
	 * @param language
	 *            profile language
	 * @param comments
	 * @return a ProfileResonse saying if the profile was created or not and the
	 *         message
	 * @throws BeanstreamApiException
	 *             if any validation fails or error occur
	 */
	private ProfileResponse createProfile(Card card, Token token,
			Address billing, CustomFields custom, String language,
			String comments) throws BeanstreamApiException {

		ProfileRequest req = new ProfileRequest(card, token, billing, custom,
				language, comments);
		ProfilesUtils.validateProfileReq(req);

		String url = BeanstreamUrls.getProfilesUrl(config.getPlatform(),
				config.getVersion());

		String response = connector.ProcessTransaction(HttpMethod.post, url,
				req);
		return gson.fromJson(response, ProfileResponse.class);

	}

	/**
	 * Retrieve a profile using the profile's ID. If you want to modify a
	 * profile you must first retrieve it using this method
	 * 
	 * @param profileId
	 *            to search
	 * @return a PaymentProfile for the given profileId
	 * @throws BeanstreamApiException
	 *             if the profile does not exist or any validation or error
	 *             occur
	 */
	public PaymentProfile getProfileById(String profileId)
			throws BeanstreamApiException {
		ProfilesUtils.validateProfileId(profileId);
		String url = BeanstreamUrls.getProfilesUrl(config.getPlatform(),
				config.getVersion(), profileId);

		String response = connector.ProcessTransaction(HttpMethod.get, url,
				null);
		return gson.fromJson(response, PaymentProfile.class);

	}

	/**
	 * Delete the profile. You must send and valid profileId
	 * 
	 * @param profileId
	 *            of the profile to delete
	 * @return ProfileResponse if successful, an BeanstreamApiException if not.
	 **/
	public ProfileResponse deleteProfileById(String profileId)
			throws BeanstreamApiException {

		ProfilesUtils.validateProfileId(profileId);
		String url = BeanstreamUrls.getProfilesUrl(config.getPlatform(),
				config.getVersion(), profileId);

		String response = connector.ProcessTransaction(HttpMethod.delete, url,
				null);
		return gson.fromJson(response, ProfileResponse.class);

	}

	/**
	 * Updates the profile. You must first retrieve the profile using
	 * ProfilesAPI.getProfileById(id)
	 * 
	 * @param profile
	 *            object to update
	 * @return ProfilePesponse if successful, an BeanstreamApiException if not.
	 **/
	public ProfileResponse updateProfile(PaymentProfile profile)
			throws BeanstreamApiException {
		Gateway.assertNotNull(profile, "profile to update is null");
		ProfilesUtils.validateProfileId(profile.getId());

		ProfilesUtils.validateBillingAddr(profile.getBilling());

		String url = BeanstreamUrls.getProfilesUrl(config.getPlatform(),
				config.getVersion(), profile.getId());

		JsonObject req = new JsonObject();
		req.add("billing", gson.toJsonTree(profile.getBilling(), Address.class));
		req.add("custom",
				gson.toJsonTree(profile.getCustom(), CustomFields.class));
		req.addProperty("language", profile.getLanguage());
		req.addProperty("comments", profile.getComments());
		String response = connector
				.ProcessTransaction(HttpMethod.put, url, req);
		return gson.fromJson(response, ProfileResponse.class);
	}

	/**
	 * Gets the cards contained on this profile. It is possible for a profile
	 * not to contain any cards if it was created using a Legato token
	 * (single-use token)
	 * 
	 * @param profileId
	 *            of the profile containing the cards
	 * @return List<Card> with all the cards for that profile
	 * @throws BeanstreamApiException
	 *             if any validation fails or any error occur
	 */
	public List<Card> getCards(String profileId) throws BeanstreamApiException {
		ProfilesUtils.validateProfileId(profileId);
		String url = BeanstreamUrls.getProfileCardsUrl(config.getPlatform(),
				config.getVersion(), profileId);

		String response = connector.ProcessTransaction(HttpMethod.get, url,
				null);
		ProfileCardsResponse pcr = gson.fromJson(response,
				ProfileCardsResponse.class);
		return pcr.getCards();

	}

	/**
	 * Get a particular card on a profile, Card IDs are their index in
	 * getCards(), starting a 1 and going up: 1, 2, 3, 4...
	 * 
	 * @param profileId
	 * @param cardId
	 * @return the Card you are looking for
	 * @throws BeanstreamApiException
	 */
	public Card getCard(String profileId, String cardId)
			throws BeanstreamApiException {

		ProfilesUtils.validateProfileId(profileId);
		Gateway.assertNotEmpty(cardId, "card id is empty");
		String url = BeanstreamUrls.getProfileCardUrl(config.getPlatform(),
				config.getVersion(), profileId, cardId);

		String response = connector.ProcessTransaction(HttpMethod.get, url,
				null);
		ProfileCardsResponse pcr = gson.fromJson(response,
				ProfileCardsResponse.class);
		Optional<Card> card = pcr.getCards().stream().findFirst();
		return card.orElse(null);

	}

	/**
	 * Updates the profile. You must first retrieve the profile using
	 * ProfilesAPI.GetProfile(id)
	 * 
	 * @param profileId
	 * @param card
	 * @return
	 * @throws BeanstreamApiException
	 */
	public ProfileResponse updateCard(String profileId, Card card)
			throws BeanstreamApiException {

		ProfilesUtils.validateProfileId(profileId);
		Gateway.assertNotNull(card, "card it to to update is empty");
		String cardId = card.getId();
		Gateway.assertNotEmpty(cardId, "card id it to update is empty");
		String url = BeanstreamUrls.getProfileCardUrl(config.getPlatform(),
				config.getVersion(), profileId, cardId);
		ProfilesUtils.validateCard(card);

		// send the card json without id
		JsonElement _card = gson.toJsonTree(card, Card.class);
		String response = connector.ProcessTransaction(HttpMethod.put, url,
				_card);
		return gson.fromJson(response, ProfileResponse.class);

	}

	/**
	 * Add a new card to the profile. It gets appended to the end of the list of
	 * cards. Make sure your Merchant account can support more cards. The
	 * default amount is 1. You can change this limit in the online Members area
	 * for Merchants located at: https://www.beanstream.com/admin/sDefault.asp
	 * and heading to Configuration -> Payment Profile Configuration
	 * 
	 * @param profileId
	 * @param card
	 * @return ProfileResponse
	 * @throws BeanstreamApiException
	 */
	public ProfileResponse addCard(String profileId, Card card)
			throws BeanstreamApiException {
		ProfilesUtils.validateProfileId(profileId);
		String url = BeanstreamUrls.getProfileCardsUrl(config.getPlatform(),
				config.getVersion(), profileId);

		ProfilesUtils.validateCard(card);
		String response = connector.ProcessTransaction(HttpMethod.post, url,
				card);
		return gson.fromJson(response, ProfileResponse.class);

	}

	/**
	 * Removes the card from the profile. Card IDs are their index in
	 * getCards(), starting a 1 and going up: 1, 2, 3, 4...
	 * 
	 * @param profileId
	 * @param cardId
	 * @return ProfileResponse
	 * @throws BeanstreamApiException
	 */
	public ProfileResponse removeCard(String profileId, String cardId)
			throws BeanstreamApiException {
		ProfilesUtils.validateProfileId(profileId);
		Gateway.assertNotEmpty(cardId, "card it to remove is empty");
		String url = BeanstreamUrls.getProfileCardUrl(config.getPlatform(),
				config.getVersion(), profileId, cardId);

		String response = connector.ProcessTransaction(HttpMethod.delete, url,
				null);
		return gson.fromJson(response, ProfileResponse.class);

	}

}
