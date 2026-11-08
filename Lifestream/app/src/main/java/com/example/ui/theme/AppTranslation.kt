package com.example.ui.theme

import android.content.Context
import org.json.JSONObject

object AppTranslation {
    // Loaded translations map: lang -> key -> value (can be String or nested Map)
    private val loadedTranslations = mutableMapOf<String, Map<String, Any>>()

    // In-memory fallback translations
    val translations = mapOf(
        "en" to mapOf(
            "app_title" to "LifeStream",
            "logistics_subtitle" to "LOGISTICS DEPLOYMENT MODEL",
            "tab_dashboard" to "Dashboard",
            "tab_feed" to "Urgent Feed",
            "tab_eligibility" to "Eligibility",
            "tab_profile" to "Profile",
            "welcome_back" to "WELCOME BACK",
            "blood_type" to "BLOOD TYPE",
            "secured_data" to "SECURED DATA",
            "can_donate" to "You Can Donate",
            "in_wait_period" to "In Wait Period",
            "switch_cooldown" to "Waiting - Locked",
            "switch_active" to "Can Pledge to Donate",
            "days_wait_time" to "WAIT TIME",
            "days_left" to "Days Remaining",
            "cooldown_progress" to "COOLDOWN PROGRESS",
            "start_quiz_card_title" to "Check if you can donate?",
            "start_quiz_card_body" to "Take our updated safety quiz to start.",
            "start_quiz_button" to "START QUIZ",
            "quiz_re_test" to "TEST AGAIN",
            "quiz_success_title" to "You Can Donate!",
            "quiz_success_body" to "All safety checks passed. Your donor profile is active and you can now pledge.",
            "quiz_fail_title" to "Please Wait to Donate",
            "quiz_fail_body" to "Medical safety guidelines say you should not donate blood right now.",
            "go_to_home" to "GO TO HOME",
            "urgent_clinic_needs" to "URGENT CLINIC NEEDS",
            "verified_blood_needed" to "Verified Blood Needed",
            "new_urgent_request" to "New Urgent Blood Request",
            "send_alert_now" to "SEND ALERT NOW",
            "dismiss" to "DISMISS",
            "pledge_button" to "PLEDGE TO DONATE",
            "auth_welcome" to "Welcome Back",
            "auth_subtitle" to "Sign in to track your donations, pledge in real-time, and manage clinical updates safely.",
            "auth_phone_label" to "Phone Number",
            "auth_pin_label" to "Access PIN (4-digits)",
            "auth_remember_me" to "Remember me",
            "auth_enter_both_error" to "Please enter both your phone number and 4-digit PIN.",
            "auth_login_btn" to "Login",
            "auth_no_account" to "Don't have an account? ",
            "auth_register_now" to "Register Now",
            "auth_create_account_title" to "Create Secure Account",
            "auth_register_as" to "REGISTER AS:",
            "auth_role_user" to "User",
            "auth_role_bloodbank" to "Blood Bank",
            "auth_full_name" to "Full Name",
            "auth_blood_bank_name" to "Blood Bank Name",
            "auth_create_pin" to "Create PIN (4-digits)",
            "auth_blood_type_group" to "BLOOD TYPE GROUP:",
            "auth_gender" to "GENDER:",
            "auth_gender_male" to "Male",
            "auth_gender_female" to "Female",
            "auth_fill_all_error" to "Please fill in all details to proceed.",
            "auth_signup_btn" to "Sign Up",
            "auth_already_have_account" to "Already have a secure account? ",
            "auth_phone_verification" to "Phone Verification",
            "auth_otp_sent_msg" to "An OTP has been transmitted via SMS! Please check your system notifications or enter the code below:",
            "auth_enter_otp" to "Enter 6-Digit OTP",
            "auth_enter_otp_error" to "Please enter the full 6-digit OTP code.",
            "auth_confirm_verification" to "CONFIRM VERIFICATION",
            "auth_back_to_signin" to "Back to Sign In"
        ),
        "hi" to mapOf(
            "app_title" to "लाइफस्ट्रीम",
            "logistics_subtitle" to "लॉजिस्टिक्स परिनियोजन मॉडल",
            "tab_dashboard" to "डैशबोर्ड",
            "tab_feed" to "आपातकालीन फ़ीड",
            "tab_eligibility" to "पात्रता",
            "tab_profile" to "प्रोफाइल",
            "welcome_back" to "स्वागत हे",
            "blood_type" to "रक्त समूह",
            "secured_data" to "सुरक्षित डेटा",
            "can_donate" to "आप दान कर सकते हैं",
            "in_wait_period" to "प्रतीक्षा अवधि में",
            "switch_cooldown" to "प्रतीक्षा करें - लॉक्ड",
            "switch_active" to "दान करने का संकल्प लें",
            "days_wait_time" to "प्रतीक्षा समय",
            "days_left" to "दिन शेष",
            "cooldown_progress" to "प्रगति की प्रतीक्षा करें",
            "start_quiz_card_title" to "क्या आप दान कर सकते हैं?",
            "start_quiz_card_body" to "शुरू करने के लिए हमारा अपडेटेड सुरक्षा क्विज़ लें।",
            "start_quiz_button" to "क्विज़ शुरू करें",
            "quiz_re_test" to "फिर से जांचें",
            "quiz_success_title" to "आप दान कर सकते हैं!",
            "quiz_success_body" to "सभी सुरक्षा जाँचें पूरी हुईं। आपका प्रोफ़ाइल सक्रिय है और आप दान कर सकते हैं।",
            "quiz_fail_title" to "कृपया दान करने के लिए प्रतीक्षा करें",
            "quiz_fail_body" to "चिकित्सा सुरक्षा दिशानिर्देश कहते हैं कि आपको अभी रक्तदान नहीं करना चाहिए।",
            "go_to_home" to "होम पर जाएं",
            "urgent_clinic_needs" to "तत्काल क्लिनिक आवश्यकताएं",
            "verified_blood_needed" to "सत्यापित रक्त की आवश्यकता",
            "new_urgent_request" to "नया आपातकालीन रक्त अनुरोध",
            "send_alert_now" to "अभी अलर्ट भेजें",
            "dismiss" to "खारिज करें",
            "pledge_button" to "दान का संकल्प लें",
            "auth_welcome" to "स्वागत हे",
            "auth_subtitle" to "रक्तदान ट्रैक करने और आपातकालीन अनुरोधों के लिए सुरक्षित लॉगिन करें।",
            "auth_phone_label" to "फ़ोन नंबर",
            "auth_pin_label" to "पिन कोड (4-अंकीय)",
            "auth_remember_me" to "मुझे याद रखें",
            "auth_enter_both_error" to "कृपया फ़ोन नंबर और पिन दोनों दर्ज करें।",
            "auth_login_btn" to "लॉगिन करें",
            "auth_no_account" to "क्या आपके पास खाता नहीं है? ",
            "auth_register_now" to "अभी रजिस्टर करें",
            "auth_create_account_title" to "नया खाता बनाएं",
            "auth_register_as" to "इस रूप में पंजीकरण करें:",
            "auth_role_user" to "उपयोगकर्ता",
            "auth_role_bloodbank" to "ब्लड बैंक",
            "auth_full_name" to "पूरा नाम",
            "auth_blood_bank_name" to "ब्लड बैंक का नाम",
            "auth_create_pin" to "सुरक्षित पिन (4-अंकीय)",
            "auth_blood_type_group" to "रक्त समूह चुनें:",
            "auth_gender" to "लिंग:",
            "auth_gender_male" to "पुरुष",
            "auth_gender_female" to "महिला",
            "auth_fill_all_error" to "कृपया सभी फ़ील्ड भरें।",
            "auth_signup_btn" to "पंजीकरण करें",
            "auth_already_have_account" to "पहले से खाता है? ",
            "auth_phone_verification" to "फ़ोन सत्यापन",
            "auth_otp_sent_msg" to "एसएमएस के माध्यम से ओटीपी भेजा गया है। ऊपर सूचना देखें और नीचे दर्ज करेंः",
            "auth_enter_otp" to "ओटीपी दर्ज करें",
            "auth_enter_otp_error" to "कृपया 6-अंकीय ओटीपी कोड दर्ज करें।",
            "auth_confirm_verification" to "सत्यापन की पुष्टि करें",
            "auth_back_to_signin" to "लॉगिन पर वापस जाएं"
        ),
        "te" to mapOf(
            "app_title" to "లైఫ్‌స్ట్రీమ్",
            "logistics_subtitle" to "లాజిస్టిక్స్ డిప్లాయ్‌మెంట్ మోడల్",
            "tab_dashboard" to "డాష్‌బోర్డ్",
            "tab_feed" to "అత్యవసర ఫీడ్",
            "tab_eligibility" to "అర్హత",
            "tab_profile" to "ప్రొఫైల్",
            "welcome_back" to "స్వాగతం",
            "blood_type" to "రక్త రకం",
            "secured_data" to "భద్రపరచబడిన డేటా",
            "can_donate" to "మీరు దానం చేయవచ్చు",
            "in_wait_period" to "వేచి ఉండే సమయం",
            "switch_cooldown" to "లాక్ చేయబడింది",
            "switch_active" to "దానం చేయడానికి ప్రతిజ్ఞ చేయవచ్చు",
            "days_wait_time" to "వేచి ఉండే సమయం",
            "days_left" to "మిగిలి ఉన్న రోజులు",
            "cooldown_progress" to "వేచి ఉండే పురోగతి",
            "start_quiz_card_title" to "మీరు దానం చేయవచ్చా?",
            "start_quiz_card_body" to "ప్రారంభించడానికి మా నవీకరించబడిన భద్రతా క్విజ్ తీసుకోండి.",
            "start_quiz_button" to "క్విజ్ ప్రారంభించండి",
            "quiz_re_test" to "మళ్లీ పరీక్షించండి",
            "quiz_success_title" to "మీరు దానం చేయవచ్చు!",
            "quiz_success_body" to "అన్ని భద్రతా తనిఖీలు విజయవంతమయ్యాయి. మీ దాత ప్రొఫైల్ సక్రియంగా ఉంది.",
            "quiz_fail_title" to "దయచేసి దానం చేయడానికి వేచి ఉండండి",
            "quiz_fail_body" to "వైద్య భద్రతా మార్గదర్శకాల ప్రకారం మీరు ఇప్పుడు రక్తాన్ని దానం చేయకూడదు.",
            "go_to_home" to "హోమ్ పేజీకి వెళ్ళు",
            "urgent_clinic_needs" to "అత్యవసర క్లినిక్ అవసరాలు",
            "verified_blood_needed" to "ధృవీకరించబడిన రక్త అవసరం",
            "new_urgent_request" to "కొత్త అత్యవసర రక్త అభ్యర్థన",
            "send_alert_now" to "అలర్ట్ పంపండి",
            "dismiss" to "రద్దు చేయి",
            "pledge_button" to "దానం చేయడానికి ప్రతిజ్ఞ",
            "auth_welcome" to "స్వాగతం",
            "auth_subtitle" to "మీ విరాళాలను ట్రాక్ చేయడానికి, నిజ-సమయంలో ప్రతిజ్ఞ చేయడానికి మరియు క్లినికల్ నవీకరణలను సురక్షితంగా నిర్వహించడానికి సైన్ ఇన్ చేయండి.",
            "auth_phone_label" to "ఫోన్ నంబర్",
            "auth_pin_label" to "యాక్సెస్ పిన్ (4-అంకెలు)",
            "auth_remember_me" to "నన్ను గుర్తుంచుకో",
            "auth_enter_both_error" to "దయచేసి మీ ఫోన్ నంబర్ మరియు 4-అంకెల పిన్ రెండింటినీ నమోదు చేయండి.",
            "auth_login_btn" to "లాగిన్",
            "auth_no_account" to "ఖాతా లేదా? ",
            "auth_register_now" to "ఇప్పుడే నమోదు చేసుకోండి",
            "auth_create_account_title" to "సురక్షిత ఖాతాను సృష్టించండి",
            "auth_register_as" to "ఇలా నమోదు చేసుకోండి:",
            "auth_role_user" to "వినియోగదారు",
            "auth_role_bloodbank" to "రక్త నిధి",
            "auth_full_name" to "పూర్తి పేరు",
            "auth_blood_bank_name" to "రక్త నిధి పేరు",
            "auth_create_pin" to "పిన్ సృష్టించండి (4-అంకెలు)",
            "auth_blood_type_group" to "రక్త రకం సమూహం:",
            "auth_gender" to "లింగం:",
            "auth_gender_male" to "పురుషుడు",
            "auth_gender_female" to "స్త్రీ",
            "auth_fill_all_error" to "దయచేసి కొనసాగడానికి అన్ని వివరాలను పూరించండి.",
            "auth_signup_btn" to "సైన్ అప్",
            "auth_already_have_account" to "ఇప్పటికే ఖాతా ఉందా? ",
            "auth_phone_verification" to "ఫోన్ ధృవీకరణ",
            "auth_otp_sent_msg" to "SMS ద్వారా ఒక OTP పంపబడింది! దయచేసి మీ సిస్టమ్ నోటిఫికేషన్‌లను తనిఖీ చేయండి లేదా దిగువ కోడ్‌ను నమోదు చేయండి:",
            "auth_enter_otp" to "6-అంకెల OTPని నమోదు చేయండి",
            "auth_enter_otp_error" to "దయచేసి పూర్తి 6-అంకెల OTP కోడ్‌ను నమోదు చేయండి.",
            "auth_confirm_verification" to "ధృవీకరణను నిర్ధారించండి",
            "auth_back_to_signin" to "సైన్ ఇన్‌కి తిరిగి వెళ్ళు"
        ),
        "ta" to mapOf(
            "app_title" to "லைஃப்ஸ்ட்ரீம்",
            "logistics_subtitle" to "லாஜிஸ்டிக்ஸ் வரிசைப்படுத்தல் மாதிரி",
            "tab_dashboard" to "டாஷ்போர்டு",
            "tab_feed" to "அவஸர ஊட்டம்",
            "tab_eligibility" to "தகுதி",
            "tab_profile" to "சுயவிவரம்",
            "welcome_back" to "வரவேற்கிறோம்",
            "blood_type" to "இரத்த வகை",
            "secured_data" to "பாதுகாக்கப்பட்ட தரவு",
            "can_donate" to "தானம் செய்யலாம்",
            "in_wait_period" to "காத்திருப்பு காலம்",
            "switch_cooldown" to "லாக் செய்யப்பட்டுள்ளது",
            "switch_active" to "தானம் செய்ய உறுதிமொழி",
            "days_wait_time" to "காத்திருப்பு காலம்",
            "days_left" to "மீதமுள்ள நாட்கள்",
            "cooldown_progress" to "கூல்டவுன் முன்னேற்றம்",
            "start_quiz_card_title" to "தானம் செய்ய முடியுமா?",
            "start_quiz_card_body" to "தொடங்குவதற்கு எங்கள் தகுதி வினாடி வினாவை எடுக்கவும்.",
            "start_quiz_button" to "விதியைத் தொடங்கு",
            "quiz_re_test" to "மீண்டும் சோதிக்கவும்",
            "quiz_success_title" to "நீங்கள் தானம் செய்ய முடியும்!",
            "quiz_success_body" to "பாதுகாப்பு சோதனைகள் அனைத்தும் வெற்றிகரமாக முடிந்தது. நீங்கள் தானம் செய்யத் தயாராகலாம்.",
            "quiz_fail_title" to "தயவுசெய்து காத்திருக்கவும்",
            "quiz_fail_body" to "மருத்துவ பாதுகாப்பு விதிமுறைகளின்படி நீங்கள் இப்போது இரத்தம் வழங்கக்கூடாது.",
            "go_to_home" to "முகப்புக்குச் செல்லவும்",
            "urgent_clinic_needs" to "அவசர மருத்துவ தேவை",
            "verified_blood_needed" to "சரிபார்க்கப்பட்ட இரத்த தேவை",
            "new_urgent_request" to "புதிய அவசர இரத்தக் கோரிக்கை",
            "send_alert_now" to "இப்போது எச்சரிக்கை செய்",
            "dismiss" to "நிராகரி",
            "pledge_button" to "தானம் செய்ய உறுதிமொழி எடுக்கவும்",
            "auth_welcome" to "வரவேற்கிறோம்",
            "auth_subtitle" to "உங்கள் நன்கொடைகளைக் கண்காணிக்கவும், நிகழ்நேரத்தில் உறுதிமொழி எடுக்கவும், மருத்துவப் புதுப்பிப்புகளைப் பாதுகாப்பாக நிர்வகிக்கவும் உள்நுழையவும்.",
            "auth_phone_label" to "தொலைபேசி எண்",
            "auth_pin_label" to "அணுகல் பின் (4-இலக்கங்கள்)",
            "auth_remember_me" to "என்னை நினைவில் கொள்",
            "auth_enter_both_error" to "உங்கள் தொலைபேசி எண் மற்றும் 4-இலக்க பின் இரண்டையும் உள்ளிடவும்.",
            "auth_login_btn" to "உள்நுழை",
            "auth_no_account" to "கணக்கு இல்லையா? ",
            "auth_register_now" to "இப்போது பதிவு செய்யுங்கள்",
            "auth_create_account_title" to "பாதுகாப்பான கணக்கை உருவாக்கவும்",
            "auth_register_as" to "இவ்வாறு பதிவு செய்க:",
            "auth_role_user" to "பயனர்",
            "auth_role_bloodbank" to "இரத்த வங்கி",
            "auth_full_name" to "முழு பெயர்",
            "auth_blood_bank_name" to "இரத்த வங்கியின் பெயர்",
            "auth_create_pin" to "பின்னை உருவாக்கவும் (4-இலக்கங்கள்)",
            "auth_blood_type_group" to "இரத்த வகை பிரிவு:",
            "auth_gender" to "பாலினம்:",
            "auth_gender_male" to "ஆண்",
            "auth_gender_female" to "பெண்",
            "auth_fill_all_error" to "தொடர அனைத்து விவரங்களையும் பூர்த்தி செய்யவும்.",
            "auth_signup_btn" to "பதிவு செய்",
            "auth_already_have_account" to "ஏற்கனவே கணக்கு உள்ளதா? ",
            "auth_phone_verification" to "தொலைபேசி சரிபார்ப்பு",
            "auth_otp_sent_msg" to "எஸ்எம்எஸ் மூலம் ஒரு ஓடிபி அனுப்பப்பட்டுள்ளது! தயவுசெய்து உங்கள் அறிவிப்புகளை சரிபார்க்கவும் அல்லது கீழே குறியீட்டை உள்ளிடவும்:",
            "auth_enter_otp" to "6-இலக்க ஓடிபியை உள்ளிடவும்",
            "auth_enter_otp_error" to "தயவுசெய்து முழுமையான 6-இலக்க ஓடிபி குறியீட்டை உள்ளிடவும்.",
            "auth_confirm_verification" to "சரிபார்ப்பை உறுதிப்படுத்தவும்",
            "auth_back_to_signin" to "உள்நுழைவுக்குத் திரும்பு"
        )
    )

    fun init(context: Context) {
        val languages = listOf("en", "hi", "te", "ta")
        for (lang in languages) {
            try {
                val jsonString = context.assets.open("$lang.json").bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)
                val map = mutableMapOf<String, Any>()
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.get(key)
                    if (value is JSONObject) {
                        val nestedMap = mutableMapOf<String, String>()
                        val nestedKeys = value.keys()
                        while (nestedKeys.hasNext()) {
                            val nk = nestedKeys.next()
                            nestedMap[nk] = value.getString(nk)
                        }
                        map[key] = nestedMap
                    } else {
                        map[key] = value.toString()
                    }
                }
                loadedTranslations[lang] = map
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun translate(key: String, lang: String): String {
        val langMap = loadedTranslations[lang] ?: loadedTranslations["en"]
        val value = langMap?.get(key) ?: translations[lang]?.get(key) ?: translations["en"]?.get(key)
        return value?.toString() ?: key
    }

    fun translateQuiz(nestedKey: String, lang: String): String {
        val langMap = loadedTranslations[lang] ?: loadedTranslations["en"]
        val quizObj = langMap?.get("quiz")
        if (quizObj is Map<*, *>) {
            val valStr = quizObj[nestedKey]
            if (valStr != null) return valStr.toString()
        }
        
        // Dynamic in-memory quick lookups as fallbacks for specific keys
        return when (nestedKey) {
            "q1" -> if (lang == "hi") "क्या आपकी उम्र कम से कम 18 वर्ष है?" else "Are you at least 18 years old?"
            "q1_desc" -> if (lang == "hi") "रक्तदान करने के लिए, आपके स्वास्थ्य और सुरक्षा के लिए आपकी आयु 18 वर्ष या उससे अधिक होनी चाहिए।" else "To donate blood, you must be 18 years or older for your health and safety."
            "q2" -> if (lang == "hi") "क्या आपका वजन 50 किलोग्राम या उससे अधिक है?" else "Do you weigh 50 kg or more?"
            "q2_desc" -> if (lang == "hi") "आपका वजन कम से कम 50 किलोग्राम होना चाहिए ताकि रक्तदान के बाद आपको चक्कर न आएं।" else "You must weigh at least 50 kg so that you do not feel dizzy after donating blood."
            "yes" -> if (lang == "hi") "हाँ" else if (lang == "te") "అవును" else "YES"
            "no" -> if (lang == "hi") "नहीं" else if (lang == "te") "లేదు" else "NO"
            "step" -> if (lang == "hi") "चरण" else if (lang == "te") "దశ" else "Step"
            "of" -> if (lang == "hi") "का" else if (lang == "te") "యొక్క" else "of"
            "quick_check" -> if (lang == "hi") "त्वरित पात्रता जाँच" else "Quick Eligibility Check"
            "success_body" -> translate("quiz_success_body", lang)
            "fail_body" -> translate("quiz_fail_body", lang)
            else -> nestedKey
        }
    }
}
