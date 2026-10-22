const { MasterData } = require('./master-data.model');

const DEFAULT_MASTER_DATA = [
  // Ashrams
  { code: 'BARWALA', category: 'ASHRAM', labels: { en: 'Satlok Ashram Barwala', hi: 'सतलोक आश्रम बरवाला' }, location: { city: 'Hisar', state: 'Haryana' }, order: 1 },
  { code: 'ROHTAK', category: 'ASHRAM', labels: { en: 'Satlok Ashram Rohtak', hi: 'सतलोक आश्रम रोहतक' }, location: { city: 'Rohtak', state: 'Haryana' }, order: 2 },
  { code: 'BHIWANI', category: 'ASHRAM', labels: { en: 'Satlok Ashram Bhiwani', hi: 'सतलोक आश्रम भिवानी' }, location: { city: 'Bhiwani', state: 'Haryana' }, order: 3 },
  { code: 'SHAMLI', category: 'ASHRAM', labels: { en: 'Satlok Ashram Shamli', hi: 'सतलोक आश्रम शामली' }, location: { city: 'Shamli', state: 'Uttar Pradesh' }, order: 4 },
  { code: 'BETUL', category: 'ASHRAM', labels: { en: 'Satlok Ashram Betul', hi: 'सतलोक आश्रम बैतूल' }, location: { city: 'Betul', state: 'Madhya Pradesh' }, order: 5 },
  { code: 'DHURI', category: 'ASHRAM', labels: { en: 'Satlok Ashram Dhuri', hi: 'सतलोक आश्रम धूरी' }, location: { city: 'Sangrur', state: 'Punjab' }, order: 6 },
  { code: 'MUNDKA', category: 'ASHRAM', labels: { en: 'Satlok Ashram Mundka', hi: 'सतलोक आश्रम मुंडका' }, location: { city: 'Delhi', state: 'Delhi' }, order: 7 },
  { code: 'KHAMANON', category: 'ASHRAM', labels: { en: 'Satlok Ashram Khamanon', hi: 'सतलोक आश्रम खमानों' }, location: { city: 'Fatehgarh Sahib', state: 'Punjab' }, order: 8 },
  { code: 'OTHER_ASHRAM', category: 'ASHRAM', labels: { en: 'Other Ashram / Location', hi: 'अन्य आश्रम / स्थान' }, order: 99 },

  // Purposes
  { code: 'SATSANG', category: 'PURPOSE', labels: { en: 'Satsang (Spiritual Discourse)', hi: 'सत्संग' }, order: 1 },
  { code: 'SEVA', category: 'PURPOSE', labels: { en: 'Seva (Selfless Service)', hi: 'सेवा' }, order: 2 },
  { code: 'BHANDARA', category: 'PURPOSE', labels: { en: 'Bhandara (Community Meal)', hi: 'भंडारा' }, order: 3 },
  { code: 'PROGRAMME', category: 'PURPOSE', labels: { en: 'Ashram Event / Programme', hi: 'आश्रम समागम / कार्यक्रम' }, order: 4 },
  { code: 'NAAM_DAAN', category: 'PURPOSE', labels: { en: 'Naam Daan Related Visit', hi: 'नाम दान संबंधी यात्रा' }, order: 5 },
  { code: 'OTHER_PURPOSE', category: 'PURPOSE', labels: { en: 'Other', hi: 'अन्य' }, order: 99 },

  // Seva Types
  { code: 'BHANDARA_SEVA', category: 'SEVA_TYPE', labels: { en: 'Bhandara Seva (Kitchen & Meal Service)', hi: 'भंडारा सेवा' }, order: 1 },
  { code: 'SAFAI_SEVA', category: 'SEVA_TYPE', labels: { en: 'Sanitation & Cleanliness (Safai Seva)', hi: 'सफाई सेवा' }, order: 2 },
  { code: 'VYAVASTHA_SEVA', category: 'SEVA_TYPE', labels: { en: 'Discipline & Crowd Management (Vyavastha)', hi: 'व्यवस्था / अनुशासन सेवा' }, order: 3 },
  { code: 'JAL_SEVA', category: 'SEVA_TYPE', labels: { en: 'Water Distribution (Jal Seva)', hi: 'जल सेवा' }, order: 4 },
  { code: 'SECURITY_SEVA', category: 'SEVA_TYPE', labels: { en: 'Premises Security', hi: 'सुरक्षा सेवा' }, order: 5 },
  { code: 'IT_MEDIA_SEVA', category: 'SEVA_TYPE', labels: { en: 'IT, Sound & Broadcasting', hi: 'ध्वनि एवं प्रसारण सेवा' }, order: 6 },
  { code: 'OTHER_SEVA', category: 'SEVA_TYPE', labels: { en: 'Other Seva', hi: 'अन्य सेवा' }, order: 99 }
];

class MasterDataService {
  async ensureSeeded() {
    const count = await MasterData.countDocuments();
    if (count === 0) {
      await MasterData.insertMany(DEFAULT_MASTER_DATA);
      console.log('[MASTER-DATA] Seeded default master data successfully.');
    }
  }

  async getAllMasterData() {
    await this.ensureSeeded();
    const records = await MasterData.find({ isActive: true }).sort({ category: 1, order: 1 }).lean();

    return {
      ashrams: records.filter(r => r.category === 'ASHRAM').map(this.formatRecord),
      purposes: records.filter(r => r.category === 'PURPOSE').map(this.formatRecord),
      sevaTypes: records.filter(r => r.category === 'SEVA_TYPE').map(this.formatRecord)
    };
  }

  formatRecord(r) {
    return {
      code: r.code,
      labels: r.labels,
      location: r.location,
      order: r.order
    };
  }
}

module.exports = new MasterDataService();
