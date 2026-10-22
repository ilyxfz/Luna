package com.luna.cycle.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

val LocalLunaLanguage = staticCompositionLocalOf { "en" }

private val translations = mapOf(
    "Today" to pair("Aujourd'hui", "اليوم"),
    "Calendar" to pair("Calendrier", "التقويم"),
    "Insights" to pair("Analyses", "الرؤى"),
    "You" to pair("Profil", "ملفك"),
    "Good morning" to pair("Bonjour", "صباح الخير"),
    "Your cycle" to pair("Votre cycle", "دورتك"),
    "YOUR CYCLE" to pair("VOTRE CYCLE", "دورتك"),
    "DAY" to pair("JOUR", "اليوم"),
    "Next period" to pair("Prochaines règles", "الدورة القادمة"),
    "Cycle length" to pair("Durée du cycle", "طول الدورة"),
    "days" to pair("jours", "أيام"),
    "day" to pair("jour", "يوم"),
    "Today's insight" to pair("Votre aperçu du jour", "نظرة اليوم"),
    "A little note for you" to pair("Un petit mot pour vous", "رسالة صغيرة لك"),
    "Your day" to pair("Votre journée", "يومك"),
    "entry" to pair("entrée", "تسجيل"),
    "entries" to pair("entrées", "تسجيلات"),
    "this cycle" to pair("ce cycle", "في هذه الدورة"),
    "How are you feeling?" to pair("Comment vous sentez-vous ?", "كيف تشعرين اليوم؟"),
    "Add symptoms, mood or a note" to pair("Ajoutez symptômes, humeur ou note", "أضيفي الأعراض أو المزاج أو ملاحظة"),
    "Log today" to pair("Noter aujourd'hui", "سجّلي اليوم"),
    "Your calendar" to pair("Votre calendrier", "تقويمك"),
    "A gentle view of your cycle" to pair("Un aperçu serein de votre cycle", "نظرة هادئة على دورتك"),
    "Cycle overview" to pair("Aperçu du cycle", "ملخص الدورة"),
    "Estimated" to pair("Estimé", "تقديري"),
    "Period length" to pair("Durée des règles", "مدة الدورة"),
    "Logged days" to pair("Jours consignés", "الأيام المسجلة"),
    "total" to pair("au total", "الإجمالي"),
    "Your daily check-ins will appear here." to pair("Vos suivis quotidiens apparaîtront ici.", "ستظهر تسجيلاتك اليومية هنا."),
    "Period" to pair("Règles", "الدورة"),
    "Fertile" to pair("Fertile", "الخصوبة"),
    "Logged" to pair("Noté", "مسجل"),
    "Your insights" to pair("Vos analyses", "رؤاك"),
    "Getting to know your rhythm" to pair("Mieux comprendre votre rythme", "تعرّفي على إيقاع دورتك"),
    "CYCLE REGULARITY" to pair("RÉGULARITÉ DU CYCLE", "انتظام الدورة"),
    "Based on the start dates you have logged" to pair("D'après les dates de début enregistrées", "استنادًا إلى تواريخ البداية المسجلة"),
    "Cycle at a glance" to pair("Le cycle en bref", "نظرة سريعة على الدورة"),
    "Average cycle length" to pair("Durée moyenne du cycle", "متوسط طول الدورة"),
    "Not enough data" to pair("Données insuffisantes", "بيانات غير كافية"),
    "Not rated" to pair("Non évalué", "غير مقيّم"),
    "Your look" to pair("Votre style", "مظهر التطبيق"),
    "Theme and accent" to pair("Thème et couleur", "السمة واللون"),
    "Accent color" to pair("Couleur d'accent", "اللون البارز"),
    "Dark mode" to pair("Mode sombre", "الوضع الداكن"),
    "Language" to pair("Langue", "اللغة"),
    "English" to pair("Anglais", "الإنجليزية"),
    "French" to pair("Français", "الفرنسية"),
    "Arabic" to pair("Arabe", "العربية"),
    "Cycle estimates" to pair("Estimations du cycle", "تقديرات الدورة"),
    "For awareness only" to pair("À titre indicatif", "للتوعية فقط"),
    "Custom" to pair("Personnalisé", "مخصص"),
    "Moving average" to pair("Moyenne mobile", "المتوسط المتحرك"),
    "Last 3 cycles" to pair("3 derniers cycles", "آخر 3 دورات"),
    "Last 6 cycles" to pair("6 derniers cycles", "آخر 6 دورات"),
    "Home screen" to pair("Écran d'accueil", "الشاشة الرئيسية"),
    "Show and reorder cards" to pair("Afficher et réorganiser les cartes", "إظهار البطاقات وترتيبها"),
    "Cycle dial" to pair("Roue du cycle", "مؤشر الدورة"),
    "Daily tip" to pair("Conseil du jour", "نصيحة اليوم"),
    "Daily log" to pair("Suivi du jour", "التسجيل اليومي"),
    "Cycle stats" to pair("Statistiques du cycle", "إحصاءات الدورة"),
    "Calendar predictions" to pair("Prévisions du calendrier", "توقعات التقويم"),
    "Opt in to add dates to Android Calendar" to pair("Ajouter les dates au calendrier Android", "إضافة التواريخ إلى تقويم Android"),
    "Sync next 3 predicted cycles" to pair("Synchroniser les 3 prochains cycles", "مزامنة الدورات الثلاث القادمة"),
    "Privacy shortcuts" to pair("Raccourcis de confidentialité", "اختصارات الخصوصية"),
    "Choose how Luna appears" to pair("Choisissez l'apparence de Luna", "اختاري مظهر لونا"),
    "Quick hide gesture" to pair("Geste de masquage rapide", "إيماءة الإخفاء السريع"),
    "Double-tap the Today heading to show a neutral notes screen" to pair("Touchez deux fois le titre du jour pour afficher des notes neutres", "انقري مرتين على عنوان اليوم لعرض شاشة ملاحظات محايدة"),
    "Discreet notifications" to pair("Notifications discrètes", "إشعارات خاصة"),
    "Replace cycle details with your custom text" to pair("Remplacer les détails du cycle par votre texte", "استبدال تفاصيل الدورة بنصك المخصص"),
    "Protect app preview" to pair("Protéger l'aperçu de l'application", "حماية معاينة التطبيق"),
    "Hide the journal from screenshots and app switcher previews" to pair("Masquer le journal des captures et de l'écran récent", "إخفاء السجل من لقطات الشاشة ومعاينة التطبيقات"),
    "Launcher name and icon" to pair("Nom et icône de l'application", "اسم التطبيق وأيقونته"),
    "Changing this adds the selected app name to your launcher." to pair("Le nom choisi apparaîtra dans votre lanceur.", "سيظهر الاسم المحدد في مشغل التطبيقات."),
    "Haptic feedback" to pair("Retour haptique", "الاستجابة اللمسية"),
    "Gentle tactile feedback on controls" to pair("Retour tactile discret sur les commandes", "استجابة لمسية خفيفة عند استخدام الأزرار"),
    "Accessibility" to pair("Accessibilité", "إمكانية الوصول"),
    "Reading and motion" to pair("Lecture et animations", "القراءة والحركة"),
    "Text size:" to pair("Taille du texte :", "حجم النص:"),
    "Bold text" to pair("Texte en gras", "نص عريض"),
    "Increase text weight across the app" to pair("Renforcer le texte dans toute l'application", "زيادة سماكة النص في التطبيق"),
    "Reduce motion" to pair("Réduire les animations", "تقليل الحركة"),
    "Use minimal screen transitions" to pair("Réduire les transitions d'écran", "استخدام انتقالات بسيطة بين الشاشات"),
    "Automatic data purge" to pair("Suppression automatique", "الحذف التلقائي للبيانات"),
    "On-device history" to pair("Historique sur l'appareil", "السجل على الجهاز"),
    "Never" to pair("Jamais", "أبدًا"),
    "A little about you" to pair("Quelques réglages", "إعداداتك"),
    "Your settings, always private" to pair("Vos réglages restent privés", "إعداداتك خاصة دائمًا"),
    "Notification privacy" to pair("Confidentialité des notifications", "خصوصية الإشعارات"),
    "Text and timing" to pair("Texte et horaires", "النص والتوقيت"),
    "Notification title" to pair("Titre de notification", "عنوان الإشعار"),
    "Notification text" to pair("Texte de notification", "نص الإشعار"),
    "Alert time (24-hour, HH:mm)" to pair("Heure d'alerte (24 h, HH:mm)", "وقت التنبيه (24 ساعة، HH:mm)"),
    "Quiet hours" to pair("Heures silencieuses", "ساعات الهدوء"),
    "Start" to pair("Début", "البداية"),
    "End" to pair("Fin", "النهاية"),
    "Daily check-in" to pair("Suivi quotidien", "تسجيل يومي"),
    "Flow" to pair("Flux", "التدفق"),
    "How would you describe it?" to pair("Comment le décririez-vous ?", "كيف تصفينه؟"),
    "Symptoms" to pair("Symptômes", "الأعراض"),
    "Select all that apply" to pair("Sélectionnez tout ce qui s'applique", "اختاري كل ما ينطبق"),
    "Mood" to pair("Humeur", "المزاج"),
    "What's your emotional weather?" to pair("Quel temps fait-il dans votre esprit ?", "كيف هو مزاجك؟"),
    "Water" to pair("Eau", "الماء"),
    "Sleep quality" to pair("Qualité du sommeil", "جودة النوم"),
    "Movement" to pair("Mouvement", "الحركة"),
    "What felt good today?" to pair("Qu'est-ce qui vous a fait du bien ?", "ما النشاط الذي شعرتِ معه بالراحة؟"),
    "Cervical mucus" to pair("Glaire cervicale", "مخاط عنق الرحم"),
    "Optional" to pair("Facultatif", "اختياري"),
    "Sexual activity" to pair("Activité sexuelle", "النشاط الجنسي"),
    "Optional and private" to pair("Facultatif et privé", "اختياري وخاص"),
    "A note to yourself" to pair("Une note pour vous", "ملاحظة لنفسك"),
    "Anything else on your mind?" to pair("Autre chose à noter ?", "هل هناك شيء آخر تودين تدوينَه؟"),
    "Save check-in" to pair("Enregistrer", "حفظ التسجيل"),
    "Last period started" to pair("Début des dernières règles", "بداية آخر دورة"),
    "Save" to pair("Enregistrer", "حفظ"),
    "Cancel" to pair("Annuler", "إلغاء"),
    "Skip" to pair("Passer", "تخطي"),
    "Continue" to pair("Continuer", "متابعة"),
    "Get started" to pair("Commencer", "ابدئي"),
    "A softer way to track" to pair("Un suivi plus doux", "طريقة ألطف للتتبع"),
    "Private by design" to pair("La confidentialité avant tout", "خصوصيتك أولًا"),
    "Your cycle, your way" to pair("Votre cycle, à votre rythme", "دورتك، بطريقتك"),
    "Pastel" to pair("Pastel", "باستيل"),
    "Warm earth" to pair("Terre chaude", "ألوان ترابية"),
    "High contrast" to pair("Contraste élevé", "تباين عالٍ"),
    "Monochrome" to pair("Monochrome", "أحادي اللون"),
    "Rose" to pair("Rose", "وردي"),
    "Terracotta" to pair("Terracotta", "طوبي"),
    "Violet" to pair("Violet", "بنفسجي"),
    "Sage" to pair("Sauge", "مريمية"),
    "Blue" to pair("Bleu", "أزرق"),
    "Graphite" to pair("Graphite", "رمادي داكن"),
    "Luna" to pair("Luna", "لونا"),
    "Notes" to pair("Notes", "ملاحظات"),
    "Planner" to pair("Agenda", "مخطط"),
    "Private by design" to pair("Confidentialité intégrée", "الخصوصية جزء من التصميم"),
    "Create encrypted backup" to pair("Créer une sauvegarde chiffrée", "إنشاء نسخة احتياطية مشفرة"),
    "Restore encrypted backup" to pair("Restaurer une sauvegarde chiffrée", "استعادة نسخة احتياطية مشفرة"),
    "Rotate database encryption key" to pair("Renouveler la clé de chiffrement", "تغيير مفتاح تشفير قاعدة البيانات"),
    "Encrypted device-to-device transfer" to pair("Transfert chiffré entre appareils", "نقل مشفر بين الأجهزة"),
    "Create encrypted backup" to pair("Créer une sauvegarde chiffrée", "إنشاء نسخة احتياطية مشفرة"),
    "Your body, your data" to pair("Votre corps, vos données", "جسدك وبياناتك"),
    "Daily Notes" to pair("Notes du jour", "ملاحظات يومية"),
    "A quiet place for your thoughts." to pair("Un espace calme pour vos pensées.", "مساحة هادئة لأفكارك."),
    "A little reminder" to pair("Un petit rappel", "تذكير صغير"),
    "Things to remember" to pair("À ne pas oublier", "أشياء لا تنسيها"),
    "Tap the title twice to return." to pair("Touchez deux fois le titre pour revenir.", "انقري العنوان مرتين للعودة."),
    "Cycle regularity" to pair("Régularité du cycle", "انتظام الدورة"),
    "Cycle regularity" to pair("Régularité du cycle", "انتظام الدورة"),
    "Cycle comparison" to pair("Comparaison des cycles", "مقارنة الدورات"),
    "Current cycle" to pair("Cycle actuel", "الدورة الحالية"),
    "Previous-cycle average" to pair("Moyenne des cycles précédents", "متوسط الدورات السابقة"),
    "Correlation patterns" to pair("Liens observés", "أنماط الارتباط"),
    "No symptoms logged in the current cycle yet." to pair("Aucun symptôme noté pour ce cycle.", "لم تُسجل أعراض في الدورة الحالية بعد."),
    "Intensity combines reported symptoms and flow. Empty days are not treated as zero." to pair("L'intensité combine les symptômes et le flux. Les jours sans saisie ne comptent pas comme zéro.", "تجمع الشدة بين الأعراض والتدفق. الأيام غير المسجلة لا تُحسب كصفر."),
    "Cycle length" to pair("Durée du cycle", "طول الدورة"),
    "Your period is expected in about" to pair("Vos règles sont prévues dans environ", "يُتوقع بدء الدورة خلال"),
    "days until your next period" to pair("jours avant vos prochaines règles", "أيام حتى الدورة القادمة"),
    "Next period" to pair("Prochaines règles", "الدورة القادمة"),
    "Cramps" to pair("Crampes", "تقلصات"),
    "Bloating" to pair("Ballonnements", "انتفاخ"),
    "Headache" to pair("Mal de tête", "صداع"),
    "Acne" to pair("Acné", "حب الشباب"),
    "Fatigue" to pair("Fatigue", "إرهاق"),
    "Tender breasts" to pair("Seins sensibles", "ألم الثدي"),
    "Backache" to pair("Mal de dos", "ألم الظهر"),
    "Calm" to pair("Calme", "هادئ"),
    "Happy" to pair("Heureuse", "سعيدة"),
    "Anxious" to pair("Anxieuse", "قلقة"),
    "Irritable" to pair("Irritable", "سريعة الانفعال"),
    "Low energy" to pair("Peu d'énergie", "طاقة منخفضة"),
    "Moody" to pair("Humeur changeante", "متقلبة المزاج"),
    "Spotting" to pair("Légères pertes", "تنقيط"),
    "Light" to pair("Léger", "خفيف"),
    "Medium" to pair("Modéré", "متوسط"),
    "Heavy" to pair("Abondant", "غزير"),
    "Rest" to pair("Repos", "راحة"),
    "Walk" to pair("Marche", "مشي"),
    "Yoga" to pair("Yoga", "يوغا"),
    "Workout" to pair("Entraînement", "تمرين"),
    "Protected" to pair("Protégé", "مع حماية"),
    "Unprotected" to pair("Sans protection", "دون حماية"),
    "Other" to pair("Autre", "أخرى"),
    "Dry" to pair("Sec", "جاف"),
    "Sticky" to pair("Collant", "لزج"),
    "Creamy" to pair("Crémeux", "كريمي"),
    "Watery" to pair("Liquide", "مائي"),
    "Egg white" to pair("Blanc d'œuf", "بياض البيض"),
    "No" to pair("Non", "لا"),
    "Yes" to pair("Oui", "نعم"),
    "Follicular phase" to pair("Phase folliculaire", "المرحلة الجريبية"),
    "Menstrual phase" to pair("Phase menstruelle", "مرحلة الحيض"),
    "Luteal phase" to pair("Phase lutéale", "المرحلة الأصفرية"),
    "Ovulation day" to pair("Jour de l'ovulation", "يوم الإباضة"),
    "A little more in tune with you" to pair("Un peu plus à l'écoute de vous-même", "تناغم أكبر مع جسدك"),
    "Your period is expected in about" to pair("Vos règles sont prévues dans environ", "يُتوقع بدء الدورة خلال"),
    "days until your next period" to pair("jours avant vos prochaines règles", "أيام حتى الدورة القادمة"),
    "A little more in tune with you" to pair("Un peu plus à l'écoute de vous-même", "تناغم أكبر مع جسدك"),
    "A little note for you" to pair("Un petit mot pour vous", "رسالة صغيرة لك"),
    "Rest when you need to. A warm compress and gentle stretching may help you feel more comfortable." to pair("Reposez-vous si nécessaire. Une compresse chaude et des étirements doux peuvent apporter du confort.", "استريحي عند الحاجة. قد تساعد الكمادات الدافئة والتمدد اللطيف على الشعور بالراحة."),
    "Energy may begin to return. Try a new recipe, a walk, or a movement routine that feels good." to pair("Votre énergie peut revenir. Essayez une nouvelle recette, une promenade ou un mouvement qui vous plaît.", "قد تبدأ طاقتك بالعودة. جربي وصفة جديدة أو المشي أو حركة تشعرك بالراحة."),
    "Stay hydrated and choose movement that feels right for you. Cycle predictions are estimates, not contraception." to pair("Hydratez-vous et choisissez un mouvement adapté. Les prévisions ne sont pas une contraception.", "حافظي على شرب الماء واختاري حركة تناسبك. توقعات الدورة ليست وسيلة لمنع الحمل."),
    "A steady bedtime and nourishing meals can support your wellbeing in the days before your next period." to pair("Un rythme de sommeil régulier et des repas nourrissants peuvent soutenir votre bien-être avant vos règles.", "قد يساعد انتظام النوم والوجبات المغذية على دعم صحتك قبل الدورة القادمة."),
    "A quiet place for your thoughts." to pair("Un espace calme pour vos pensées.", "مساحة هادئة لأفكارك."),
    "Cycle Comparison" to pair("Comparaison des cycles", "مقارنة الدورات"),
    "Cycle comparison" to pair("Comparaison des cycles", "مقارنة الدورات"),
    "Symptom intensity by cycle day" to pair("Intensité des symptômes par jour du cycle", "شدة الأعراض حسب يوم الدورة"),
    "Symptom patterns" to pair("Tendances des symptômes", "أنماط الأعراض"),
    "From your check-ins" to pair("D'après vos suivis", "من تسجيلاتك"),
    "Wellbeing patterns" to pair("Tendances du bien-être", "أنماط العافية"),
    "Local estimates from your logs" to pair("Estimations locales à partir de vos données", "تقديرات محلية من سجلاتك"),
    "Log a few days to discover patterns in how you feel." to pair("Notez quelques jours pour découvrir vos tendances.", "سجلي بضعة أيام لاكتشاف أنماط شعورك."),
    "Log water, sleep and symptoms on at least 8 days to explore possible associations." to pair("Notez l'eau, le sommeil et les symptômes pendant au moins 8 jours pour explorer les liens.", "سجلي الماء والنوم والأعراض لمدة 8 أيام على الأقل لاستكشاف الارتباطات."),
    "These are descriptive associations, not evidence that one factor causes another." to pair("Ces associations sont descriptives et ne prouvent pas de lien de causalité.", "هذه ارتباطات وصفية ولا تثبت أن أحد العوامل يسبب الآخر."),
    "Your cycle pattern may be changing" to pair("Le rythme de votre cycle semble évoluer", "قد يتغير نمط دورتك"),
    "No symptoms logged in the current cycle yet." to pair("Aucun symptôme noté pour ce cycle.", "لم تُسجل أعراض في الدورة الحالية بعد."),
    "Cycle at a glance" to pair("Le cycle en bref", "نظرة سريعة على الدورة"),
    "This cycle" to pair("Ce cycle", "هذه الدورة"),
    "Daily check-in reminder" to pair("Rappel de suivi quotidien", "تذكير بالتسجيل اليومي"),
    "A gentle nudge each day" to pair("Un petit rappel quotidien", "تذكير يومي لطيف"),
    "Cycle & fertile window alerts" to pair("Alertes du cycle et de fertilité", "تنبيهات الدورة والخصوبة"),
    "A quiet heads-up before your period" to pair("Un rappel discret avant vos règles", "تنبيه هادئ قبل الدورة"),
    "Daily medication reminder" to pair("Rappel quotidien de médicament", "تذكير يومي بالدواء"),
    "A private nudge for your pill" to pair("Un rappel privé pour votre pilule", "تذكير خاص بحبوب الدواء"),
    "Biometric lock" to pair("Verrouillage biométrique", "قفل ببصمة الوجه أو الإصبع"),
    "Unlock with your fingerprint" to pair("Déverrouiller avec votre empreinte", "فتح القفل ببصمة الإصبع"),
    "Encrypted on this device" to pair("Chiffré sur cet appareil", "مشفّر على هذا الجهاز"),
    "Save a private, portable copy" to pair("Enregistrer une copie privée et portable", "حفظ نسخة خاصة قابلة للنقل"),
    "Merge saved check-ins" to pair("Fusionner les suivis enregistrés", "دمج التسجيلات المحفوظة"),
    "Daily Notes" to pair("Notes du jour", "ملاحظات يومية"),
    "Create encrypted backup" to pair("Créer une sauvegarde chiffrée", "إنشاء نسخة احتياطية مشفرة"),
    "Restore encrypted backup" to pair("Restaurer une sauvegarde chiffrée", "استعادة نسخة احتياطية مشفرة"),
    "Rotate key" to pair("Renouveler la clé", "تغيير المفتاح"),
    "Your journal is stored on this device in an encrypted database. Luna never sells or shares your health data." to pair("Votre journal est chiffré sur cet appareil. Luna ne vend ni ne partage vos données de santé.", "يُحفظ سجلك مشفرًا على هذا الجهاز. لا تبيع لونا بياناتك الصحية ولا تشاركها."),
    "Luna is for cycle awareness and is not a contraceptive or a substitute for medical advice." to pair("Luna aide à suivre le cycle, sans remplacer une contraception ni un avis médical.", "لونا لتتبع الدورة وليست وسيلة لمنع الحمل أو بديلًا عن الاستشارة الطبية."),
    "Custom text replaces cycle-specific details on notifications." to pair("Votre texte personnalisé remplace les détails du cycle dans les notifications.", "يستبدل النص المخصص تفاصيل الدورة في الإشعارات."),
    "Choose how Luna appears" to pair("Choisissez l'apparence de Luna", "اختاري مظهر لونا"),
    "Haptic strength:" to pair("Intensité haptique :", "شدة اللمس:"),
    "Cycle stats" to pair("Statistiques du cycle", "إحصاءات الدورة"),
    "Set up a fingerprint or face unlock first." to pair("Configurez d'abord une empreinte ou la reconnaissance faciale.", "أعدّي بصمة الإصبع أو فتح القفل بالوجه أولًا."),
    "Quick hide" to pair("Masquage rapide", "إخفاء سريع"),
    "Wellbeing patterns" to pair("Tendances du bien-être", "أنماط العافية"),
    "Export health summary" to pair("Exporter le résumé de santé", "تصدير ملخص صحي"),
    "Your cycle pattern may be changing" to pair("Le rythme de votre cycle semble évoluer", "قد يتغير نمط دورتك"),
    "Some logged cycles were shorter than 21 days or longer than 35 days. " to pair("Certains cycles enregistrés ont duré moins de 21 jours ou plus de 35 jours. ", "كانت بعض الدورات المسجلة أقصر من 21 يومًا أو أطول من 35 يومًا. "),
    "Your next period is later than the estimate. " to pair("Vos prochaines règles sont plus tardives que prévu. ", "الدورة القادمة متأخرة عن الموعد المتوقع. "),
    "Cycle variation can be common. Consider discussing persistent changes with a healthcare professional." to pair("Les variations peuvent être courantes. Parlez des changements persistants à un professionnel de santé.", "قد يكون تفاوت الدورة طبيعيًا. استشيري مختصًا صحيًا إذا استمرت التغييرات."),
    "Log flow or symptoms to compare cycles over time." to pair("Notez le flux ou les symptômes pour comparer vos cycles.", "سجلي التدفق أو الأعراض لمقارنة الدورات بمرور الوقت."),
    "Chart comparing daily symptom intensity for the latest cycle and previous cycles" to pair("Graphique comparant l'intensité quotidienne des symptômes du cycle actuel et des précédents", "مخطط يقارن شدة الأعراض اليومية في الدورة الحالية والدورات السابقة"),
    "  Log period days across cycles to see your score" to pair("  Notez vos règles sur plusieurs cycles pour voir votre score", "  سجلي أيام الدورة عبر عدة دورات لعرض نتيجتك"),
    "A softer way to track" to pair("Un suivi plus doux", "طريقة ألطف للتتبع"),
    "Log your cycle, symptoms, mood and daily wellbeing at your own pace." to pair("Suivez votre cycle, vos symptômes, votre humeur et votre bien-être à votre rythme.", "تابعي دورتك وأعراضك ومزاجك وعافيتك اليومية على وتيرتك."),
    "Your journal is encrypted on this device. Optional app lock and private reminders are in Settings." to pair("Votre journal est chiffré sur cet appareil. Le verrouillage et les rappels privés sont dans les réglages.", "سجلك مشفر على هذا الجهاز. ستجدين قفل التطبيق والتذكيرات الخاصة في الإعدادات."),
    "Explore your calendar, patterns and reminders. Predictions are estimates and never a substitute for medical advice." to pair("Explorez le calendrier, les tendances et les rappels. Les prévisions sont indicatives et ne remplacent pas un avis médical.", "استكشفي التقويم والأنماط والتذكيرات. التوقعات تقديرية ولا تغني عن المشورة الطبية."),
    "Choose a passphrase with at least 8 characters. You’ll need it to restore this backup." to pair("Choisissez une phrase secrète d'au moins 8 caractères. Elle sera nécessaire pour restaurer cette sauvegarde.", "اختاري عبارة مرور من 8 أحرف على الأقل. ستحتاجينها لاستعادة هذه النسخة."),
    "Backup passphrase" to pair("Phrase secrète de sauvegarde", "عبارة مرور النسخة الاحتياطية"),
    "Choose file" to pair("Choisir un fichier", "اختيار ملف"),
    "Enter the passphrase used when this backup was created. Matching dates will be replaced." to pair("Saisissez la phrase secrète utilisée pour cette sauvegarde. Les dates correspondantes seront remplacées.", "أدخلي عبارة المرور المستخدمة لإنشاء النسخة. ستُستبدل السجلات ذات التواريخ المطابقة."),
    "Restore" to pair("Restaurer", "استعادة"),
    "Rotate encryption key?" to pair("Renouveler la clé de chiffrement ?", "هل تريدين تغيير مفتاح التشفير؟"),
    "Luna will re-encrypt the local database with a new device-protected key. Your logs remain in place. Create an encrypted backup first as a precaution." to pair("Luna rechiffrera la base locale avec une nouvelle clé protégée par l'appareil. Vos suivis seront conservés. Créez une sauvegarde chiffrée avant de continuer.", "ستعيد لونا تشفير قاعدة البيانات المحلية بمفتاح جديد محمي بالجهاز. ستبقى سجلاتك كما هي. أنشئي نسخة احتياطية مشفرة أولًا."),
    "Rotate key" to pair("Renouveler la clé", "تغيير المفتاح"),
    "1 year" to pair("1 an", "سنة واحدة"),
    "3 years" to pair("3 ans", "3 سنوات"),
    "5 years" to pair("5 ans", "5 سنوات"),
    "Encrypted device-to-device transfer" to pair("Transfert chiffré entre appareils", "نقل مشفر بين الأجهزة"),
    "Use the passphrase-protected backup above" to pair("Utilisez la sauvegarde protégée par phrase secrète ci-dessus", "استخدمي النسخة الاحتياطية المحمية بعبارة المرور أعلاه"),
    "Re-encrypt your local journal with a new key" to pair("Chiffrer à nouveau votre journal local avec une nouvelle clé", "إعادة تشفير سجلك المحلي بمفتاح جديد"),
    "A quiet place for your thoughts." to pair("Un espace calme pour vos pensées.", "مساحة هادئة لأفكارك."),
    "Things to remember" to pair("À ne pas oublier", "أشياء لا تنسيها"),
    "A little reminder" to pair("Un petit rappel", "تذكير صغير"),
    "Tap the title twice to return." to pair("Touchez deux fois le titre pour revenir.", "انقري العنوان مرتين للعودة."),
    "Period length" to pair("Durée des règles", "مدة الدورة"),
    "Cycle overview" to pair("Aperçu du cycle", "ملخص الدورة"),
    "Estimated" to pair("Estimé", "تقديري"),
    "Logged days" to pair("Jours consignés", "الأيام المسجلة"),
    "total" to pair("au total", "الإجمالي"),
    "Period" to pair("Règles", "الدورة"),
    "Fertile" to pair("Fertile", "الخصوبة"),
    "Logged" to pair("Noté", "مسجل"),
    "Your insights" to pair("Vos analyses", "رؤاك"),
    "Getting to know your rhythm" to pair("Mieux comprendre votre rythme", "تعرّفي على إيقاع دورتك"),
    "Based on the start dates you have logged" to pair("D'après les dates de début enregistrées", "استنادًا إلى تواريخ البداية المسجلة"),
    "Cycle comparison" to pair("Comparaison des cycles", "مقارنة الدورات"),
    "Symptom intensity by cycle day" to pair("Intensité des symptômes par jour du cycle", "شدة الأعراض حسب يوم الدورة"),
    "Symptom patterns" to pair("Tendances des symptômes", "أنماط الأعراض"),
    "From your check-ins" to pair("D'après vos suivis", "من تسجيلاتك"),
    "Wellbeing patterns" to pair("Tendances du bien-être", "أنماط العافية"),
    "Local estimates from your logs" to pair("Estimations locales à partir de vos données", "تقديرات محلية من سجلاتك"),
    "Your day" to pair("Votre journée", "يومك"),
    "entry" to pair("entrée", "تسجيل"),
    "entries" to pair("entrées", "تسجيلات"),
    "this cycle" to pair("ce cycle", "في هذه الدورة"),
    "Current cycle" to pair("Cycle actuel", "الدورة الحالية"),
    "Log a few days to discover patterns in how you feel." to pair("Notez quelques jours pour découvrir vos tendances.", "سجلي بضعة أيام لاكتشاف أنماط شعورك."),
    "Log flow or symptoms to compare cycles over time." to pair("Notez le flux ou les symptômes pour comparer vos cycles.", "سجلي التدفق أو الأعراض لمقارنة الدورات بمرور الوقت."),
    "Log water, sleep and symptoms on at least 8 days to explore possible associations." to pair("Notez l'eau, le sommeil et les symptômes pendant au moins 8 jours pour explorer les liens.", "سجلي الماء والنوم والأعراض لمدة 8 أيام على الأقل لاستكشاف الارتباطات."),
    "These are descriptive associations, not evidence that one factor causes another." to pair("Ces associations sont descriptives et ne prouvent pas de lien de causalité.", "هذه ارتباطات وصفية ولا تثبت أن أحد العوامل يسبب الآخر."),
    "No symptoms logged in the current cycle yet." to pair("Aucun symptôme noté pour ce cycle.", "لم تُسجل أعراض في الدورة الحالية بعد."),
    "Intensity combines reported symptoms and flow. Empty days are not treated as zero." to pair("L'intensité combine les symptômes et le flux. Les jours sans saisie ne comptent pas comme zéro.", "تجمع الشدة بين الأعراض والتدفق. الأيام غير المسجلة لا تُحسب كصفر."),
    "Calendar predictions" to pair("Prévisions du calendrier", "توقعات التقويم"),
    "Opt in to add dates to Android Calendar" to pair("Ajouter les dates au calendrier Android", "إضافة التواريخ إلى تقويم Android"),
    "Sync next 3 predicted cycles" to pair("Synchroniser les 3 prochains cycles", "مزامنة الدورات الثلاث القادمة"),
    "Daily medication reminder" to pair("Rappel quotidien de médicament", "تذكير يومي بالدواء"),
    "Cycle & fertile window alerts" to pair("Alertes du cycle et de fertilité", "تنبيهات الدورة والخصوبة"),
    "Daily check-in reminder" to pair("Rappel de suivi quotidien", "تذكير بالتسجيل اليومي"),
    "Biometric lock" to pair("Verrouillage biométrique", "قفل ببصمة الوجه أو الإصبع"),
    "Launcher name and icon" to pair("Nom et icône de l'application", "اسم التطبيق وأيقونته"),
    "Changing this adds the selected app name to your launcher." to pair("Le nom choisi apparaîtra dans votre lanceur.", "سيظهر الاسم المحدد في مشغل التطبيقات."),
    "Your settings, always private" to pair("Vos réglages restent privés", "إعداداتك خاصة دائمًا"),
    "Anything else on your mind?" to pair("Autre chose à noter ?", "هل هناك شيء آخر تودين تدوينه؟"),
    "Custom text replaces cycle-specific details on notifications." to pair("Votre texte personnalisé remplace les détails du cycle dans les notifications.", "يستبدل النص المخصص تفاصيل الدورة في الإشعارات."),
    "Quiet hours" to pair("Heures silencieuses", "ساعات الهدوء"),
    "Notification privacy" to pair("Confidentialité des notifications", "خصوصية الإشعارات"),
    "Text and timing" to pair("Texte et horaires", "النص والتوقيت"),
    "Notification title" to pair("Titre de notification", "عنوان الإشعار"),
    "Notification text" to pair("Texte de notification", "نص الإشعار"),
    "Alert time (24-hour, HH:mm)" to pair("Heure d'alerte (24 h, HH:mm)", "وقت التنبيه (24 ساعة، HH:mm)"),
    "Start" to pair("Début", "البداية"),
    "End" to pair("Fin", "النهاية"),
    "Use the passphrase-protected backup above" to pair("Utilisez la sauvegarde protégée par phrase secrète ci-dessus", "استخدمي النسخة الاحتياطية المحمية بعبارة المرور أعلاه"),
    "A private nudge for your pill" to pair("Un rappel privé pour votre pilule", "تذكير خاص بحبوب الدواء"),
    "A gentle nudge each day" to pair("Un petit rappel quotidien", "تذكير يومي لطيف"),
    "A quiet heads-up before your period" to pair("Un rappel discret avant vos règles", "تنبيه هادئ قبل الدورة"),
    "Your body, your data" to pair("Votre corps, vos données", "جسدك وبياناتك"),
    "Cycle regularity" to pair("Régularité du cycle", "انتظام الدورة"),
    "Not enough data" to pair("Données insuffisantes", "بيانات غير كافية"),
    "Export health summary" to pair("Exporter le résumé de santé", "تصدير ملخص صحي"),
    "Your period is expected in about" to pair("Vos règles sont prévues dans environ", "يُتوقع بدء الدورة خلال"),
    "CYCLE PROGRESS" to pair("PROGRESSION DU CYCLE", "تقدم الدورة"),
    "Cycle at a glance" to pair("Le cycle en bref", "نظرة سريعة على الدورة"),
    "Day" to pair("Jour", "اليوم"),
    "WATER" to pair("EAU", "الماء"),
    "WEEK" to pair("SEMAINE", "الأسبوع"),
    "HOUR" to pair("HEURE", "الساعة"),
    "OK" to pair("OK", "حسنًا"),
    "Take a moment for yourself." to pair("Prenez un moment pour vous.", "خذي لحظة لنفسك."),
    "1 year" to pair("1 an", "سنة واحدة"),
    "3 years" to pair("3 ans", "3 سنوات"),
    "5 years" to pair("5 ans", "5 سنوات"),
    "Cycle regularity" to pair("Régularité du cycle", "انتظام الدورة"),
    "Current cycle" to pair("Cycle actuel", "الدورة الحالية"),
    "Period duration" to pair("Durée des règles", "مدة الدورة"),
    "Follicular phase" to pair("Phase folliculaire", "المرحلة الجريبية"),
    "Menstrual phase" to pair("Phase menstruelle", "مرحلة الحيض"),
    "Luteal phase" to pair("Phase lutéale", "المرحلة الأصفرية"),
    "Ovulation day" to pair("Jour de l'ovulation", "يوم الإباضة"),
    "Pastel" to pair("Pastel", "باستيل"),
    "Warm earth" to pair("Terre chaude", "ألوان ترابية"),
    "High contrast" to pair("Contraste élevé", "تباين عالٍ"),
    "Monochrome" to pair("Monochrome", "أحادي اللون"),
    "Haptic strength:" to pair("Intensité haptique :", "شدة اللمس:"),
    "Notify me" to pair("Me notifier", "أرسلِي لي إشعارًا"),
    "Quiet hours" to pair("Heures silencieuses", "ساعات الهدوء"),
    "A note to yourself" to pair("Une note pour vous", "ملاحظة لنفسك"),
    "Anything else on your mind?" to pair("Autre chose à noter ?", "هل هناك شيء آخر تودين تدوينه؟"),
    "Your period is expected in about" to pair("Vos règles sont prévues dans environ", "يُتوقع بدء الدورة خلال"),
    "CYCLE PROGRESS" to pair("PROGRESSION DU CYCLE", "تقدم الدورة"),
    "Today's insight" to pair("Votre aperçu du jour", "نظرة اليوم"),
    "Cycle regularity" to pair("Régularité du cycle", "انتظام الدورة"),
    "Privacy shortcuts" to pair("Raccourcis de confidentialité", "اختصارات الخصوصية"),
    "Choose how Luna appears" to pair("Choisissez l'apparence de Luna", "اختاري مظهر لونا"),
    "Daily check-in" to pair("Suivi quotidien", "تسجيل يومي"),
    "How would you describe it?" to pair("Comment le décririez-vous ?", "كيف تصفينه؟"),
    "Your daily check-ins will appear here." to pair("Vos suivis quotidiens apparaîtront ici.", "ستظهر تسجيلاتك اليومية هنا."),
    "No symptoms logged in the current cycle yet." to pair("Aucun symptôme noté pour ce cycle.", "لم تُسجل أعراض في الدورة الحالية بعد."),
    "Last period started" to pair("Début des dernières règles", "بداية آخر دورة"),
    "Some logged cycles were shorter than 21 days or longer than 35 days. " to pair("Certains cycles enregistrés ont duré moins de 21 jours ou plus de 35 jours. ", "كانت بعض الدورات المسجلة أقصر من 21 يومًا أو أطول من 35 يومًا. "),
    "Your next period is later than the estimate. " to pair("Vos prochaines règles sont plus tardives que prévu. ", "الدورة القادمة متأخرة عن الموعد المتوقع. "),
    "Cycle variation can be common. Consider discussing persistent changes with a healthcare professional." to pair("Les variations peuvent être courantes. Parlez des changements persistants à un professionnel de santé.", "قد يكون تفاوت الدورة طبيعيًا. استشيري مختصًا صحيًا إذا استمرت التغييرات."),
    "Your cycle, your way" to pair("Votre cycle, à votre rythme", "دورتك، بطريقتك"),
    "Private by design" to pair("La confidentialité avant tout", "خصوصيتك أولًا"),
    "Set up a fingerprint or face unlock first." to pair("Configurez d'abord une empreinte ou la reconnaissance faciale.", "أعدّي بصمة الإصبع أو فتح القفل بالوجه أولًا."),
    "Create encrypted backup" to pair("Créer une sauvegarde chiffrée", "إنشاء نسخة احتياطية مشفرة"),
    "Restore encrypted backup" to pair("Restaurer une sauvegarde chiffrée", "استعادة نسخة احتياطية مشفرة"),
    "A gentle nudge each day" to pair("Un petit rappel quotidien", "تذكير يومي لطيف"),
    "A quiet heads-up before your period" to pair("Un rappel discret avant vos règles", "تنبيه هادئ قبل الدورة"),
    "A private nudge for your pill" to pair("Un rappel privé pour votre pilule", "تذكير خاص بحبوب الدواء"),
    "Your body, your data" to pair("Votre corps, vos données", "جسدك وبياناتك"),
    "Private by design" to pair("Confidentialité intégrée", "الخصوصية جزء من التصميم"),
    "Protected" to pair("Protégé", "مع حماية"),
    "Unprotected" to pair("Sans protection", "دون حماية"),
    "Average from logged period starts: " to pair("Moyenne calculée à partir des débuts de règles notés : ", "المتوسط محسوب من تواريخ بداية الدورة المسجلة: "),
    " cycles (as available)." to pair(" cycles (selon les données disponibles).", " دورات (حسب البيانات المتاحة)."),
    "Custom cycle length: " to pair("Durée personnalisée du cycle : ", "طول الدورة المخصص: "),
    "  Log period days across cycles to see your score" to pair("  Notez vos règles sur plusieurs cycles pour voir votre score", "  سجلي أيام الدورة عبر عدة دورات لعرض نتيجتك"),
    " period duration" to pair(" durée des règles", " مدة الدورة"),
    "Understood" to pair("Compris", "مفهوم"),
    "Menstrual" to pair("Menstruelle", "الحيض"),
    "Follicular" to pair("Folliculaire", "الجريبية"),
    "Ovulation" to pair("Ovulation", "الإباضة"),
    "Luteal" to pair("Lutéale", "الأصفر"),
    "Encrypted backup created." to pair("Sauvegarde chiffrée créée.", "تم إنشاء النسخة الاحتياطية المشفرة."),
    "Backup failed: " to pair("Échec de la sauvegarde : ", "فشل إنشاء النسخة الاحتياطية: "),
    "Unable to write file." to pair("Impossible d'écrire le fichier.", "تعذرت كتابة الملف."),
    "Calendar sync failed: " to pair("Échec de la synchronisation du calendrier : ", "فشلت مزامنة التقويم: "),
    "Unable to sync." to pair("Impossible de synchroniser.", "تعذرت المزامنة."),
    "Calendar access is needed to add cycle dates." to pair("L'accès au calendrier est nécessaire pour ajouter les dates du cycle.", "يلزم الوصول إلى التقويم لإضافة تواريخ الدورة."),
    "Set up a fingerprint or face unlock first." to pair("Configurez d'abord une empreinte ou la reconnaissance faciale.", "أعدّي بصمة الإصبع أو فتح القفل بالوجه أولًا."),
    "Use at least 8 characters for the passphrase." to pair("Utilisez au moins 8 caractères pour la phrase secrète.", "استخدمي 8 أحرف على الأقل لعبارة المرور."),
    "Restore failed: " to pair("Échec de la restauration : ", "فشلت الاستعادة: "),
    "Check the backup and passphrase." to pair("Vérifiez la sauvegarde et la phrase secrète.", "تحققي من النسخة الاحتياطية وعبارة المرور."),
    "Database encryption key rotated." to pair("La clé de chiffrement de la base a été renouvelée.", "تم تغيير مفتاح تشفير قاعدة البيانات."),
    "Key rotation failed: " to pair("Échec du renouvellement de la clé : ", "فشل تغيير المفتاح: "),
    "The existing key was retained." to pair("L'ancienne clé a été conservée.", "تم الاحتفاظ بالمفتاح الحالي."),
    "Added " to pair("Ajout de ", "تمت إضافة "),
    " cycle dates to your calendar." to pair(" dates du cycle à votre calendrier.", " تواريخ للدورة إلى تقويمك."),
    "Restored " to pair("Restauration de ", "تمت استعادة "),
    " check-ins." to pair(" suivis.", " تسجيلات.")
)

private fun pair(french: String, arabic: String) = mapOf("fr" to french, "ar" to arabic)

fun localizeLunaText(value: String, language: String): String {
    if (language == "en") return value
    translations[value]?.get(language)?.let { return it }

    val days = Regex("^(In )?(\\d+) days?( until your next period)?$")
    val match = days.matchEntire(value)
    if (match != null) {
        val count = match.groupValues[2]
        val suffix = match.groupValues[3].isNotEmpty()
        return when (language) {
            "fr" -> if (suffix) "$count jours avant vos prochaines règles" else if (match.groupValues[1].isNotEmpty()) "Dans $count jours" else "$count jours"
            "ar" -> if (suffix) "باقٍ $count أيام على الدورة القادمة" else if (match.groupValues[1].isNotEmpty()) "خلال $count أيام" else "$count أيام"
            else -> value
        }
    }
    val periodTip = Regex("^(Menstrual|Follicular|Ovulation|Luteal)( phase)? tip$")
    periodTip.matchEntire(value)?.let { match ->
        val phase = mapOf(
            "Menstrual" to pair("Menstruelle", "الحيض"),
            "Follicular" to pair("Folliculaire", "الجريبية"),
            "Ovulation" to pair("Ovulation", "الإباضة"),
            "Luteal" to pair("Lutéale", "الأصفر")
        ).getValue(match.groupValues[1])[language] ?: match.groupValues[1]
        return if (language == "fr") "Conseil de phase $phase" else "نصيحة مرحلة $phase"
    }
    Regex("^Custom cycle length: (\\d+) days$").matchEntire(value)?.let { match ->
        return if (language == "fr") {
            "Durée personnalisée du cycle : ${match.groupValues[1]} jours"
        } else {
            "طول الدورة المخصص: ${match.groupValues[1]} يومًا"
        }
    }
    Regex("^Haptic strength: (\\d+)%$").matchEntire(value)?.let { match ->
        return if (language == "fr") {
            "Intensité haptique : ${match.groupValues[1]} %"
        } else {
            "شدة الاستجابة اللمسية: ${match.groupValues[1]}٪"
        }
    }
    Regex("^(.+) appeared on (\\d+)% of (.+) days versus (\\d+)% on comparison days \\((\\d+) and (\\d+) days\\)\\.$")
        .matchEntire(value)?.let { match ->
            val symptom = localizeLunaText(match.groupValues[1], language)
            val factor = when (match.groupValues[3]) {
                "lower water intake" -> if (language == "fr") "les jours de faible hydratation" else "أيام انخفاض شرب الماء"
                "lower sleep quality" -> if (language == "fr") "les jours de sommeil de moindre qualité" else "أيام انخفاض جودة النوم"
                else -> match.groupValues[3]
            }
            return if (language == "fr") {
                "$symptom apparaît dans ${match.groupValues[2]} % des $factor, contre ${match.groupValues[4]} % les jours de comparaison (${match.groupValues[5]} et ${match.groupValues[6]} jours)."
            } else {
                "ظهر $symptom في ${match.groupValues[2]}٪ من $factor، مقابل ${match.groupValues[4]}٪ في أيام المقارنة (${match.groupValues[5]} و${match.groupValues[6]} يومًا)."
            }
        }
    var translated = value
    translations.entries
        .asSequence()
        .filter { (source, _) -> source.length >= 5 && translated.contains(source) }
        .sortedByDescending { it.key.length }
        .forEach { (source, variants) -> translated = translated.replace(source, variants[language] ?: source) }
    val cycleDay = Regex("^Day (\\d+)$").matchEntire(translated)
    if (cycleDay != null) {
        return if (language == "fr") "Jour ${cycleDay.groupValues[1]}" else "اليوم ${cycleDay.groupValues[1]}"
    }
    val numberOfDays = Regex("^(\\d+) days?$").matchEntire(translated)
    if (numberOfDays != null) {
        val count = numberOfDays.groupValues[1]
        return if (language == "fr") "$count jours" else "$count أيام"
    }
    val cycleTotal = Regex("^of (\\d+)$").matchEntire(translated)
    if (cycleTotal != null) {
        return if (language == "fr") "sur ${cycleTotal.groupValues[1]}" else "من ${cycleTotal.groupValues[1]}"
    }
    return translated
}

fun showLunaToast(context: Context, message: String) {
    val language = context.getSharedPreferences("luna_settings", Context.MODE_PRIVATE)
        .getString("app_language", "en") ?: "en"
    Toast.makeText(context, localizeLunaText(message, language), Toast.LENGTH_LONG).show()
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val translated = localizeLunaText(text, LocalLunaLanguage.current)
    MaterialText(
        text = translated,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}
