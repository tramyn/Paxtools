package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Proxy(proxyClass= EvidenceCodeVocabulary.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EvidenceCodeVocabularyImpl extends ControlledVocabularyImpl
	implements EvidenceCodeVocabulary
{
	public EvidenceCodeVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends EvidenceCodeVocabulary> getModelInterface() {
        return EvidenceCodeVocabulary.class;
    }
}