package de.unipotsdam.elis.webdav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import cz.topolik.fsrepo.LocalFileSystemRepository;
import de.unipotsdam.elis.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import de.unipotsdam.elis.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import de.unipotsdam.elis.chemistry.opencmis.inmemory.storedobj.impl.DocumentImpl;
import de.unipotsdam.elis.chemistry.opencmis.inmemory.storedobj.impl.DocumentVersionImpl;

public class WebdavDocumentImpl extends DocumentImpl implements VersionedDocument, DocumentVersion {
	private String decodedId;	
	

	private static Log log = LogFactoryUtil
			.getLog(WebdavDocumentImpl.class);

	public WebdavDocumentImpl(DavResource davResource) {					
		String id = WebdavIdDecoderAndEncoder.webdavToIdEncoded(davResource);			
		setDefaults(id);
		setWebdavContentDefaults();		
		setDebugProperties();			
		setWebdavContentProperties(davResource);		
		
	}

	public WebdavDocumentImpl(String encodedId) {			
		
		setDefaults(encodedId);
		setWebdavContentDefaults();		
		setDebugProperties();			
	}
	
	

	private void setDebugProperties() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		this.setCreatedAt(cal);
		this.setModifiedAt(cal);
	}

	private void setDefaults(String encodedId) {
		this.setContent(new ContentStreamImpl());
		this.setId(encodedId);
		this.setTypeId("cmis:document");
		this.decodedId = WebdavIdDecoderAndEncoder.decode(encodedId);
		String name = WebdavIdDecoderAndEncoder.encodedIdToName(encodedId);
		this.setName(name);
		String parentID =	WebdavIdDecoderAndEncoder.decodedIdToParent(decodedId);		
		this.addParentId(parentID);									
	}

	private void setWebdavContentDefaults() {
		ContentStreamImpl steam = (ContentStreamImpl) getContent();		
		steam.setFileName(this.getName());			
		steam.setLength(new BigInteger("-1"));				
		steam.setMimeType("application/octet-stream");	
		setInputStream(steam);
		this.setContent(steam);
	}
	
	public void setWebdavContentProperties(DavResource davResource) {		
		String mimeTyp = davResource.getContentType();
		Long fileLength = davResource.getContentLength();
		ContentStreamImpl steam = (ContentStreamImpl) getContent();
		steam.setFileName(this.getName());
		steam.setLength(BigInteger.valueOf(fileLength));		
		steam.setMimeType(mimeTyp);				
		setInputStream(steam);	
		setContent(steam);
	}

	private void setInputStream(ContentStreamImpl steam) {
		LocalFileSystemRepository.getWebdavRepository().setInputStream(steam, decodedId);
	}

	@Override
	public DocumentVersion addVersion(VersioningState verState, String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteVersion(DocumentVersion version) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCheckedOut() {
		return true; // liferay soll denken, editieren zu können
	}

	@Override
	public void cancelCheckOut(String user) {	
		// we don't have checkout
	}

	@Override
	public DocumentVersion checkOut(String user) {
		// we don't do anything and only have one Version
		return this;
	}

	@Override
	public void checkIn(boolean isMajor, Properties properties,
			ContentStream content, String checkinComment,
			List<String> policyIds, String user) {		
		// we don't do anything
	}

	@Override
	public List<DocumentVersion> getAllVersions() {
		// we don't do anything and only have one Version
		DocumentVersion documentVersionImpl = checkOut(null);
		return Collections.singletonList(documentVersionImpl);
	}

	@Override
	public DocumentVersion getLatestVersion(boolean major) {
		// we don't do anything and only have one Version
		return checkOut(null);
	}

	@Override
	public String getCheckedOutBy() {
//		return InMemoryServiceContext.getCallContext().getUsername();
		return null;
	}

	@Override
	public DocumentVersion getPwc() {
		return checkOut(null);
	}

	@Override
	public boolean isMajor() {
		return true;
	}

	@Override
	public boolean isPwc() {
		return true;
	}

	@Override
	public void commit(boolean isMajor) {
		// we don't do anything
		
	}

	@Override
	public void setCheckinComment(String comment) {
		// we don't do anything		
	}

	@Override
	public String getCheckinComment() {
		return "we don't check in";
	}

	@Override
	public String getVersionLabel() {
		return "1.0";
	}

	@Override
	public VersionedDocument getParentDocument() {
		return WebdavFolderImpl.computerParentDocument(decodedId);
	}

	public boolean canWrite() {		
		return true;
	}
	
	@Override
	public String getName() {
		return WebdavIdDecoderAndEncoder.encodedIdToName(this.decodedId);
	}

	public boolean exists() {
		return LocalFileSystemRepository.getWebdavRepository().exists(this);		
	}

	public boolean renameTo(WebdavDocumentImpl dstFile) {
		LocalFileSystemRepository.getWebdavRepository().rename(this.getId(), dstFile.getId());
		return true;
	}

	public void delete() {
		LocalFileSystemRepository.getWebdavRepository().deleteDirectory(this.getId());
	}
	

				
}
