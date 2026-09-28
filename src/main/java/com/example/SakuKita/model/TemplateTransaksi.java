package com.example.SakuKita.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TemplateTransaksi {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nama;
    private String jenis;
    private BigDecimal jumlah;
    private String keterangan;

    @ManyToOne 
    @JoinColumn (name = "user_id", nullable = false)
    private User user;

    @ManyToOne 
    @JoinColumn (name = "kategori_id")
    private Kategori kategori;

    public String getNamaTemplate() {
        return this.nama;
    }

    public void setNamaTemplate(String namaTemplate) {
        this.nama = namaTemplate;
    }

    public String getDeskripsi() {
        return this.keterangan;
    }

    public void setDeskripsi(String deskripsi) {
        this.keterangan = deskripsi;
    }
}
