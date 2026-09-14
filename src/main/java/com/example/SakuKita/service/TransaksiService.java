package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.TransaksiRepository;

@Service 
public class TransaksiService {
    @Autowired 
    private TransaksiRepository transaksiRepository;
    @Autowired
    private SaldoService saldoService;

    public TransaksiService(TransaksiRepository transaksiRepository, SaldoService saldoService){
        this.transaksiRepository = transaksiRepository;
        this.saldoService = saldoService;
    }

    public Transaksi pemasukan(User user, BigDecimal jumlah, String keterangan){
        Transaksi transaksi = new Transaksi();
        transaksi.setJenis("PEMASUKAN");
        transaksi.setJumlah(jumlah);
        transaksi.setKeterangan(keterangan);
        transaksi.setTanggal(LocalDateTime.now());
        transaksi.setUser(user);
        saldoService.tambahSaldo(user, jumlah);
        return transaksiRepository.save(transaksi);
    }

    public Transaksi pengeluaran(User user, BigDecimal jumlah, String keterangan){
        Transaksi transaksi = new Transaksi();
        transaksi.setJenis("PENGELUARAN");
        transaksi.setJumlah(jumlah);
        transaksi.setKeterangan(keterangan);
        transaksi.setTanggal(LocalDateTime.now());
        transaksi.setUser(user);
        saldoService.kurangiSaldo(user, jumlah);
        return transaksiRepository.save(transaksi);
    }

    public List<Transaksi> cariTransaksiUser(User user){
        return transaksiRepository.findByUser(user);
    }

    public void hapusTransaksi(Long id){
        transaksiRepository.deleteById(id);
    }
}
