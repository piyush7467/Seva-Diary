const { MasterData } = require('./master-data.model');

const DEFAULT_MASTER_DATA = [
  // 14 Ashram Master Locations (from official notice)
  { code: 'DHANANA_DHAM', category: 'ASHRAM', labels: { en: 'Shri Dhanana Dham Ashram', hi: 'श्री धनाना धाम आश्रम' }, location: { state: 'Haryana' }, order: 1 },
  { code: 'KARONTHA_DHAM', category: 'ASHRAM', labels: { en: 'Shri Karontha Dham Ashram', hi: 'श्री करौँथा धाम आश्रम' }, location: { state: 'Haryana' }, order: 2 },
  { code: 'KURUKSHETRA', category: 'ASHRAM', labels: { en: 'Kurukshetra Ashram', hi: 'कुरुक्षेत्र आश्रम' }, location: { state: 'Haryana' }, order: 3 },
  { code: 'MUNDKA', category: 'ASHRAM', labels: { en: 'Mundka Ashram, Delhi', hi: 'मुंडका आश्रम, दिल्ली' }, location: { city: 'Delhi', state: 'Delhi' }, order: 4 },
  { code: 'DHURI', category: 'ASHRAM', labels: { en: 'Dhuri Ashram, Punjab', hi: 'धुरी आश्रम, पंजाब' }, location: { city: 'Sangrur', state: 'Punjab' }, order: 5 },
  { code: 'KHAMANO', category: 'ASHRAM', labels: { en: 'Khamano Ashram, Punjab', hi: 'खमानो आश्रम, पंजाब' }, location: { state: 'Punjab' }, order: 6 },
  { code: 'SHAMLI', category: 'ASHRAM', labels: { en: 'Shamli Ashram, Uttar Pradesh', hi: 'शामली आश्रम, उत्तर प्रदेश' }, location: { city: 'Shamli', state: 'Uttar Pradesh' }, order: 7 },
  { code: 'SITAPUR', category: 'ASHRAM', labels: { en: 'Sitapur Ashram, Uttar Pradesh', hi: 'सीतापुर आश्रम, उत्तर प्रदेश' }, location: { state: 'Uttar Pradesh' }, order: 8 },
  { code: 'SOJAT', category: 'ASHRAM', labels: { en: 'Sojat Ashram, Rajasthan', hi: 'सोजत आश्रम, राजस्थान' }, location: { state: 'Rajasthan' }, order: 9 },
  { code: 'BETUL', category: 'ASHRAM', labels: { en: 'Betul Ashram, Madhya Pradesh', hi: 'बैतूल आश्रम, मध्य प्रदेश' }, location: { city: 'Betul', state: 'Madhya Pradesh' }, order: 10 },
  { code: 'INDORE', category: 'ASHRAM', labels: { en: 'Indore Ashram, Madhya Pradesh', hi: 'इंदौर आश्रम, मध्य प्रदेश' }, location: { state: 'Madhya Pradesh' }, order: 11 },
  { code: 'DHAWALAPURI', category: 'ASHRAM', labels: { en: 'Dhawalapuri Ashram, Maharashtra', hi: 'धवलपुरी आश्रम, महाराष्ट्र' }, location: { state: 'Maharashtra' }, order: 12 },
  { code: 'DHANUSHA', category: 'ASHRAM', labels: { en: 'Dhanusha Ashram, Nepal', hi: 'धनुषा आश्रम, नेपाल' }, location: { country: 'Nepal' }, order: 13 },
  { code: 'DHARWAD', category: 'ASHRAM', labels: { en: 'Dharwad Ashram, Karnataka', hi: 'धारवाड़ आश्रम, कर्नाटक' }, location: { state: 'Karnataka' }, order: 14 },
  
  // Historical / Existing locations preserved
  { code: 'BARWALA', category: 'ASHRAM', labels: { en: 'Satlok Ashram Barwala', hi: 'सतलोक आश्रम बरवाला' }, location: { city: 'Hisar', state: 'Haryana' }, order: 15 },
  { code: 'ROHTAK', category: 'ASHRAM', labels: { en: 'Satlok Ashram Rohtak', hi: 'सतलोक आश्रम रोहतक' }, location: { city: 'Rohtak', state: 'Haryana' }, order: 16 },
  { code: 'BHIWANI', category: 'ASHRAM', labels: { en: 'Satlok Ashram Bhiwani', hi: 'सतलोक आश्रम भिवानी' }, location: { city: 'Bhiwani', state: 'Haryana' }, order: 17 },
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
    for (const item of DEFAULT_MASTER_DATA) {
      await MasterData.updateOne(
        { code: item.code, category: item.category },
        { $set: item },
        { upsert: true }
      );
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
