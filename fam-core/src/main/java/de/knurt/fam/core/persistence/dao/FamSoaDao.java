package de.knurt.fam.core.persistence.dao;

import java.util.List;

import org.jcouchdb.db.Response;
import org.json.JSONObject;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.util.termsofuse.TermsOfUsePage;

/**
 * a dao for terms of use agreements.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (04/05/2012)
 */
public interface FamSoaDao {

	/**
	 * return all {@link SoaActivationDocument}s
	 * 
	 * @return all {@link SoaActivationDocument}s
	 */
	@Deprecated
	public List<SoaActivationDocument> getAllSoaActivation();

	/**
	 * return the output as needed for the view
	 * 
	 * @param docid
	 *            of the soa
	 * @return the output as needed for the view
	 */
	public JSONObject getRealSoaActivationPageDocument(String docid);

	public Response put(JSONObject jo);

	/**
	 * return all active terms of use pages for the given user as list. return
	 * an empty list if nothing is configured for this user.
	 * 
	 * @param user
	 *            the terms of user agreements are for
	 * @return all active terms of use pages for the given user as list.
	 */
	public List<TermsOfUsePage> getActiveTermsOfUsePages(User user);

	/**
	 * return a list of {@link SoaActivationDocument}s that have to be
	 * deactivated if the given {@link SoaActivationDocument} is the new one.
	 * 
	 * @param newSoaActivationDocument
	 *            as the new configuration
	 * @return a list of {@link SoaActivationDocument}s
	 */
	public List<SoaActivationDocument> getSoaActivationDocumentsForDeactivation(SoaActivationDocument newSoaActivationDocument);
}