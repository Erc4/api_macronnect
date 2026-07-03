package com.macronnect.api_macronnect.venta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "folio_sequence")
public class FolioSequence {

    @Id
    @Column(name = "seq_name", length = 50)
    private String seqName;

    @Column(name = "seq_value", nullable = false)
    private Long seqValue;

    public FolioSequence() {
    }

    public FolioSequence(String seqName, Long seqValue) {
        this.seqName = seqName;
        this.seqValue = seqValue;
    }

    public String getSeqName() {
        return seqName;
    }

    public Long getSeqValue() {
        return seqValue;
    }

    public void setSeqValue(Long seqValue) {
        this.seqValue = seqValue;
    }
}