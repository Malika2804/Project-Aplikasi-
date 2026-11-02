/**
 * Penerima data aplikasi "Siap Operasi" -> Google Sheets
 * Cara pakai:
 * 1. Buat Google Sheets baru, lalu Extensions > Apps Script.
 * 2. Hapus isi bawaan, tempel seluruh kode ini, simpan.
 * 3. Deploy > New deployment > Web app. Execute as: Me. Who has access: Anyone.
 * 4. Salin URL Web App (berakhiran /exec) dan tempel ke konstanta
 *    GOOGLE_SHEET_WEB_APP_URL di berkas siap_operasi_revisi.html.
 * 5. Setiap mengubah kode ini, lakukan Deploy > Manage deployments > Edit > New version.
 *
 * Perlindungan: skor dihitung ulang di server dari jawaban q1..q6 (bukan dipercaya dari
 * aplikasi), kode responden dan kelompok divalidasi, dan pengiriman ganda diabaikan.
 */
var NAMA_SHEET_DATA = 'Data';
var HEADERS = ['Waktu Server', 'ID Sesi', 'Kode', 'Kelompok', 'Tahap', 'Usia', 'Jenis Kelamin', 'Pendidikan',
  'Tindakan Operasi', 'Domain Selesai', 'Q1', 'Q2', 'Q3', 'Q4', 'Q5', 'Q6',
  'Skor Kecemasan', 'Skor Informasi', 'Waktu Perangkat'];

function responJson(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(ContentService.MimeType.JSON);
}

function doGet() {
  return responJson({ status: 'ok', pesan: 'Penerima data Siap Operasi aktif.' });
}

function doPost(e) {
  var lock = LockService.getScriptLock();
  try {
    lock.waitLock(20000);
    var d = JSON.parse(e.postData.contents);

    // Validasi
    if (!/^[IK]-\d{3}$/.test(String(d.kode))) return responJson({ status: 'ditolak', alasan: 'kode tidak valid' });
    if (d.kelompok !== 'Intervensi' && d.kelompok !== 'Kontrol') return responJson({ status: 'ditolak', alasan: 'kelompok tidak valid' });
    if ((d.kelompok === 'Intervensi') !== (String(d.kode).charAt(0) === 'I')) return responJson({ status: 'ditolak', alasan: 'kode tidak cocok dengan kelompok' });
    if (d.tahap !== 'Pretest' && d.tahap !== 'Posttest') return responJson({ status: 'ditolak', alasan: 'tahap tidak valid' });
    var q = [];
    for (var i = 1; i <= 6; i++) {
      var v = Number(d['q' + i]);
      if (!(v >= 1 && v <= 5) || Math.floor(v) !== v) return responJson({ status: 'ditolak', alasan: 'jawaban q' + i + ' tidak valid' });
      q.push(v);
    }
    var skorCemas = q[0] + q[1] + q[3] + q[4];
    var skorInfo = q[2] + q[5];

    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var sh = ss.getSheetByName(NAMA_SHEET_DATA) || ss.insertSheet(NAMA_SHEET_DATA);
    if (sh.getLastRow() === 0) {
      sh.appendRow(HEADERS);
      sh.setFrozenRows(1);
    }

    // Abaikan pengiriman ganda (ID Sesi + Tahap yang sama)
    var last = sh.getLastRow();
    if (last > 1) {
      var existing = sh.getRange(2, 2, last - 1, 4).getValues(); // ID Sesi, Kode, Kelompok, Tahap
      for (var r = 0; r < existing.length; r++) {
        if (String(existing[r][0]) === String(d.idSesi) && existing[r][3] === d.tahap) {
          return responJson({ status: 'duplikat' });
        }
      }
    }

    sh.appendRow([new Date(), d.idSesi, d.kode, d.kelompok, d.tahap, d.usia, d.jenisKelamin, d.pendidikan,
      d.tindakanOperasi, d.domainSelesai, q[0], q[1], q[2], q[3], q[4], q[5], skorCemas, skorInfo, d.waktu]);
    return responJson({ status: 'ok' });
  } catch (err) {
    return responJson({ status: 'error', pesan: String(err) });
  } finally {
    try { lock.releaseLock(); } catch (x) {}
  }
}

/**
 * Menjalankan fungsi ini (dari editor Apps Script) membuat sheet "Rekap" berformat lebar:
 * satu baris per responden dengan skor pretest, posttest, dan selisihnya, siap diekspor ke SPSS.
 */
function buatRekapLebar() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var data = ss.getSheetByName(NAMA_SHEET_DATA);
  if (!data || data.getLastRow() < 2) { throw new Error('Sheet Data masih kosong.'); }
  var rows = data.getRange(2, 1, data.getLastRow() - 1, HEADERS.length).getValues();
  var peta = {};
  rows.forEach(function (r) {
    var kode = r[2];
    if (!peta[kode]) peta[kode] = { kelompok: r[3], usia: r[5], jk: r[6], pend: r[7], operasi: r[8], domain: '', preC: '', preI: '', postC: '', postI: '' };
    var o = peta[kode];
    if (r[4] === 'Pretest') { o.preC = r[16]; o.preI = r[17]; }
    if (r[4] === 'Posttest') { o.postC = r[16]; o.postI = r[17]; o.domain = r[9]; }
  });
  var out = [['Kode', 'Kelompok', 'Usia', 'Jenis Kelamin', 'Pendidikan', 'Tindakan Operasi', 'Domain Selesai',
    'Pre Kecemasan', 'Post Kecemasan', 'Selisih Kecemasan (Post-Pre)', 'Pre Informasi', 'Post Informasi']];
  Object.keys(peta).sort().forEach(function (k) {
    var o = peta[k];
    var selisih = (o.preC !== '' && o.postC !== '') ? (o.postC - o.preC) : '';
    out.push([k, o.kelompok, o.usia, o.jk, o.pend, o.operasi, o.domain, o.preC, o.postC, selisih, o.preI, o.postI]);
  });
  var sh = ss.getSheetByName('Rekap') || ss.insertSheet('Rekap');
  sh.clearContents();
  sh.getRange(1, 1, out.length, out[0].length).setValues(out);
  sh.setFrozenRows(1);
}
