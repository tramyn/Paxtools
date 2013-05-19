package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
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
@Proxy(proxyClass= ExperimentalFormVocabulary.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ExperimentalFormVocabularyImpl extends ControlledVocabularyImpl
	implements ExperimentalFormVocabulary
{
	public ExperimentalFormVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends ExperimentalFormVocabulary> getModelInterface() {
        return ExperimentalFormVocabulary.class;
    }
}