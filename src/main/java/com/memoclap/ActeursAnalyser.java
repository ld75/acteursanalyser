package com.memoclap;

import com.memoclap.utils.Utils;
import jakarta.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Dependent
public class ActeursAnalyser {
    static final Logger log = LoggerFactory.getLogger(ActeursAnalyser.class);
    public static final String LIGNES_AVEC_NOMS_EN_MAJUSCULES_REGEXP = "^[A-Z0-9\\p{javaUpperCase} ]{3,}[\\w\\W]+";
    public static final String LIGNES_AVEC_NOMS_TOUT_TYPE_REGEXP = "^\\S{2,}[\\w\\p{javaUpperCase} .]*[\\w\\W]+";

    public LinkedHashMap<Integer, String> getActeursListFromFile(File text) throws Exception {
        String content = Files.readString(text.toPath(), StandardCharsets.UTF_8);
        return getActeursListFromText(content);
    }

    protected static LinkedHashMap<Integer, String> getActeursListFromText(String content) throws Exception {
        boolean isNomsEnMaj = determinerSiNomsEnMAjuscules(content);
        if (isNomsEnMaj) {
            return getNomsActeursOrdonneFromText(content, (List<String> speaker, String ligne) -> fromLineToListOfRedondances(speaker, ligne, LIGNES_AVEC_NOMS_EN_MAJUSCULES_REGEXP));
        }
        else{
            return getNomsActeursOrdonneFromText(content, (List<String> speaker, String ligne) -> fromLineToListOfRedondances(speaker, ligne, LIGNES_AVEC_NOMS_TOUT_TYPE_REGEXP));
        }
    }

    protected static LinkedHashMap<Integer, String> getNomsActeursOrdonneFromText(String content,Utils.ThrowingBiConsumer<List<String>,String,Exception> analyseLineForPossibleActors) throws Exception {
        List<String> lignes = convertirTexteEnListe(content);
        List<String> speakers = new ArrayList<>();
        for (String ligne:lignes) {
            analyseLineForPossibleActors.accept( speakers, ligne);
        }
        LinkedHashMap<Integer, String> ordreacteurs = new LinkedHashMap<>();
        speakers = extractionVraisNomsPersonnagesEtNettoyage(speakers);
        Collections.sort(speakers);
        int i=0;
        for (String speaker : speakers) {
            log.debug(speaker);
            ordreacteurs.put(i,speaker);
            i++;
        }
        return ordreacteurs;
    }

    private static List<String> convertirTexteEnListe(String content) {
        List<String> lignes = Arrays.stream(content.split("\n")).collect(Collectors.toList());
        /* TODO: quelle methode est la plus efficace ?
        List<String> LineStringArray = new ArrayList<>();
        montexte.lines().forEach((line) -> LineStringArray.add(line.trim()));
         */
        return lignes;
    }


    private static List<String> extractionVraisNomsPersonnagesEtNettoyage(List<String> speakers) {
        List<String> apparitionsMultiplesSansLesCharsSpecs = suppressionCharSpeciaux(speakers);
        //apparitionsMultiplesSansLesCharsSpecs.stream().forEach(s -> System.out.println("00000000000"+s));
        List<String> listeSansLesFauxNoms = suppressionFauxNoms(apparitionsMultiplesSansLesCharsSpecs);
        boolean choixNomsEnMajuscules = isChoixNomsEnMajuscules(listeSansLesFauxNoms);
        if (choixNomsEnMajuscules)  listeSansLesFauxNoms= suppressionParolesDeNomsMajuscules(listeSansLesFauxNoms);
        List<String> listeSansLesFauxNomsQueRedondants = conservationQueDesMultiplesOccurences(listeSansLesFauxNoms);
        if (choixNomsEnMajuscules) {
            ajoutDesActeursParlantDeConcertQuneSeuleFois(listeSansLesFauxNoms, listeSansLesFauxNomsQueRedondants);
            ajoutDesActeursParlantQuneSeuleFois(listeSansLesFauxNoms, listeSansLesFauxNomsQueRedondants);
        }
        return listeSansLesFauxNomsQueRedondants.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> suppressionFauxNoms(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        boolean choixNomsEnMajuscules = isChoixNomsEnMajuscules(apparitionsMultiplesSansLesCharsSpecs);
        List<String> listeSansLesFauxNoms;
        if (choixNomsEnMajuscules) {
            apparitionsMultiplesSansLesCharsSpecs= conserverDistinctQueNomsMajusculesAGauche(apparitionsMultiplesSansLesCharsSpecs);
            listeSansLesFauxNoms = suppressionDeNomsContenus(apparitionsMultiplesSansLesCharsSpecs);
        }
        else{
            apparitionsMultiplesSansLesCharsSpecs= conserverQueNomsAMinuscules(apparitionsMultiplesSansLesCharsSpecs);
            listeSansLesFauxNoms = suppressionDeNomsContenants(apparitionsMultiplesSansLesCharsSpecs);
        }
        return listeSansLesFauxNoms;

    }

    private static boolean isChoixNomsEnMajuscules(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        long nombreDeNomsAMajuscules = conserverDistinctQueNomsMajusculesAGauche(apparitionsMultiplesSansLesCharsSpecs).size();
        long nombreDeNomsAMinuscules = analyseNombreDeNomsAMinuscules(apparitionsMultiplesSansLesCharsSpecs);
        return isChoixNomsEnMajuscules(nombreDeNomsAMajuscules, nombreDeNomsAMinuscules);
    }

    public static boolean isChoixNomsEnMajuscules(long nombreDeNomsAMajuscules, long nombreDeNomsAMinuscules) {
        if (nombreDeNomsAMajuscules==nombreDeNomsAMinuscules) return true;
        long moy = (nombreDeNomsAMajuscules+nombreDeNomsAMinuscules)/2;
        int minimum = 2;
        if (nombreDeNomsAMajuscules<= minimum && nombreDeNomsAMinuscules>= minimum) return false;
        if (nombreDeNomsAMajuscules>= minimum && nombreDeNomsAMinuscules<= minimum) return true;
        if (nombreDeNomsAMajuscules>moy && nombreDeNomsAMinuscules<moy) return false;
        if (nombreDeNomsAMajuscules<moy && nombreDeNomsAMinuscules>moy) return true;
        return true;
    }

    static List<String> conservationQueDesMultiplesOccurences(List<String> listeSansLesFauxNoms) {
        listeSansLesFauxNoms.forEach(log::debug);
        return listeSansLesFauxNoms.stream().filter(s -> Collections.frequency(listeSansLesFauxNoms, s) > 1).collect(Collectors.toList());
    }

    private static void ajoutDesActeursParlantDeConcertQuneSeuleFois(List<String> listeSansLesFauxNoms, List<String> listeSansLesFauxNomsQueRedondants) {
        List<String> listeDePlusieursActeursParlantUneSeuleFois = getListeDePlusieursActeursParlantEnMemeTempsUneSeuleFois(listeSansLesFauxNoms);
        listeSansLesFauxNomsQueRedondants.addAll(listeDePlusieursActeursParlantUneSeuleFois);
    }

    private static void ajoutDesActeursParlantQuneSeuleFois(List<String> listeSansLesFauxNoms, List<String> listeSansLesFauxNomsQueRedondants) {
        List<String> listeDePlusieursActeursParlantUneSeuleFois = getListeAceursMajusculeParlantUneSeuleFois(listeSansLesFauxNoms);
        listeSansLesFauxNomsQueRedondants.addAll(listeDePlusieursActeursParlantUneSeuleFois);
    }

    static List<String> suppressionDeNomsContenus(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        apparitionsMultiplesSansLesCharsSpecs.sort(Comparator.reverseOrder());
        List<String> listeSansLesFauxNoms = new ArrayList<>();
        String previous=null;
        for (String possibleFauxNom : apparitionsMultiplesSansLesCharsSpecs) {
            if (!isPossibleFauxNomEstDansPreviousDoncFaux(previous, possibleFauxNom)) {
                listeSansLesFauxNoms.add(possibleFauxNom);
                previous = possibleFauxNom;}
        }
        listeSansLesFauxNoms.forEach(log::debug);
        return listeSansLesFauxNoms;

    }

    private static List<String> conserverQueNomsAMinuscules(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        apparitionsMultiplesSansLesCharsSpecs = apparitionsMultiplesSansLesCharsSpecs.stream()
                .filter(s -> s.matches("^[A-Z0-9][a-z0-9\\p{javaUpperCase} .]{2,}"))
                .filter(s -> !s.matches("^[A-Z ]{3,}"))
                .collect(Collectors.toList());
        return apparitionsMultiplesSansLesCharsSpecs;
    }

    private static List<String> suppressionDeNomsContenants(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        apparitionsMultiplesSansLesCharsSpecs= conservationQueDesMultiplesOccurences(apparitionsMultiplesSansLesCharsSpecs);
        apparitionsMultiplesSansLesCharsSpecs.sort(Comparator.naturalOrder());
        List<String> listeSansLesFauxNoms = new ArrayList<>();
        String previous=null;
        for (String possibleFauxNom : apparitionsMultiplesSansLesCharsSpecs) {
            if (!isPossibleFauxNomContientPreviousDoncEstFaux(previous, possibleFauxNom)) {
                listeSansLesFauxNoms.add(possibleFauxNom);
                previous = possibleFauxNom;}
        }
        return listeSansLesFauxNoms;
    }

    private static long analyseNombreDeNomsAMinuscules(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        List<String> nomsAMinuscules = apparitionsMultiplesSansLesCharsSpecs.stream()
                .distinct()
                .filter(s -> s.matches("^[A-Z0-9][a-z0-9éè ]{2,}")).collect(Collectors.toList());
        List<String> nomsSansContenants = suppressionDeNomsContenus(nomsAMinuscules);
        return nomsSansContenants.size();
    }

    private static List<String> getListeDePlusieursActeursParlantEnMemeTempsUneSeuleFois(List<String> listeSansLesFauxNoms) {
        return listeSansLesFauxNoms.stream()
                .filter(s->s.contains(" ET "))
                .filter(s->Collections.frequency(listeSansLesFauxNoms,s)==1)
                .collect(Collectors.toList());
    }

    private static List<String> getListeAceursMajusculeParlantUneSeuleFois(List<String> listeSansLesFauxNoms) {
        return listeSansLesFauxNoms.stream()
                .filter(s->s.matches("[A-Z0-9\\p{javaUpperCase} ]+"))
                .filter(s->Collections.frequency(listeSansLesFauxNoms,s)==1)
                .collect(Collectors.toList());
    }
    protected static void fromLineToListOfRedondances(List<String> speakers, String ligne, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(ligne);
        while (matcher.find()) {
            String speaker = matcher.group().trim();
            speakers.add(speaker);
            if (speaker.contains(" ")) {
                String nextOccurence = speaker.substring(0, speaker.lastIndexOf(" ") + 1);
                if (nextOccurence.length()>3) fromLineToListOfRedondances(speakers, nextOccurence,regexp);
            }
        }

    }

    protected static List<String> suppressionParolesDeNomsMajuscules(List<String> listeSansLesFauxNoms) {
        return listeSansLesFauxNoms.stream()
                .map(ActeursAnalyser::suppressionParolesDeNomMajuscule)
                .collect(Collectors.toList());
    }

    protected static List<String> conserverDistinctQueNomsMajusculesAGauche(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        List<String> nomsAMajuscules = apparitionsMultiplesSansLesCharsSpecs.stream()
                .distinct()
                .filter(ActeursAnalyser::nestpasToutMajNiToutEntreParentheses)
                .map(s -> supprimerParenthesesParole(s.trim()))
                .filter(ActeursAnalyser::isNomSuiviDeParoles)
                .map(s -> recupererPremieresMajuscules(s.trim()))
                .filter(ActeursAnalyser::isOnlyMajusculeLine)
                .distinct()
                .collect(Collectors.toList());
        nomsAMajuscules.forEach(log::debug);
        return nomsAMajuscules;
    }

    protected static List<String> conserverQueLignesAvecNomsMajusculesAGauche(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        List<String> nomsAMajuscules = apparitionsMultiplesSansLesCharsSpecs.stream()
                .distinct()
                .filter(ActeursAnalyser::nestpasToutMajNiToutEntreParentheses)
                .map(s -> supprimerParenthesesParole(s.trim()))
                .filter(ActeursAnalyser::isNomSuiviDeParoles)
                .map(s -> recupererPremieresMajuscules(s.trim()))
                .filter(ActeursAnalyser::isOnlyMajusculeLine)
                .collect(Collectors.toList());
        nomsAMajuscules.forEach(log::debug);
        return nomsAMajuscules;
    }
    private static boolean isOnlyMajusculeLine(String s) {
        return !s.matches(".*\\s\\p{javaUpperCase}");
    }

    private static boolean isNomSuiviDeParoles(String s) {
        if (s.contains("(")) return false;
        return s.matches(LIGNES_AVEC_NOMS_EN_MAJUSCULES_REGEXP);
    }

    private static boolean nestpasToutMajNiToutEntreParentheses(String s) {
        if (s.matches("^\\(.*$")) return false;
        return !s.matches("^[A-Z0-9\\s\\W]*$");
    }

    public static boolean isPossibleFauxNomEstDansPreviousDoncFaux(String previous, String possibleFauxNom) {
        return previous != null && previous.length() > possibleFauxNom.length() && previous.contains(possibleFauxNom);
    }

    public static boolean isPossibleFauxNomContientPreviousDoncEstFaux(String previous, String possibleFauxNom) {
        return previous != null && possibleFauxNom.length() > previous.length() && possibleFauxNom.contains(previous) && !possibleFauxNom.contains(" et ");
    }

    public static String recupererPremieresMajuscules(String stringTrimme) {
        stringTrimme = stringTrimme.replaceAll("\\(\\w\\)","");
        String out = stringTrimme.replaceAll("(^[A-Z0-9\\p{javaUpperCase} ]{3,})(\\s?\\W?\\s?\\p{L}{1}\\W?[\\p{javaLowerCase}\\s]+.*)", "$1");
        out = out.replaceAll("(^[A-Z0-9\\p{javaUpperCase} ]{3,})(\\s+\\p{L}*)", "$1");
        return out.trim();
    }

    protected static String suppressionParolesDeNomMajuscule(String s) {
        s= s.replaceAll("[\\s\\W]?[^\\p{javaUpperCase}]\\p{javaUpperCase}?\\W?\\p{javaLowerCase}+", "");
        s= s.replaceAll("\\s+\\W+$", "");
        return s.trim();
    }

    public static String supprimerParenthesesParole(String s) {
        //	Pattern pattern = Pattern.compile("\\(.*\\)");
        Pattern pattern = Pattern.compile("\\(.*?\\)\\s*");
        Matcher matcher = pattern.matcher(s);
        List<String> matcherlist = new ArrayList<>();
        while (matcher.find()) {
            matcherlist.add(matcher.group());
        }
        matcherlist.sort(Comparator.comparingInt(String::length));
        for (String match : matcherlist) {
            s = s.replace(match, "");
        }
        return s.trim();
    }

    protected static List<String> suppressionCharSpeciaux(List<String> speakers) {
        return speakers.stream()
                .filter(s -> s.matches("[\\p{javaLowerCase}0-9\\p{javaUpperCase}\\W]*"))
                .map(s -> s.replaceAll(":", ""))
                .map(s -> s.replaceAll(",", ""))
                .map(s -> s.replaceAll("  ", " "))
                .map(s -> s.replaceAll("\\.", " "))
                .filter(s -> s.matches(".*[A-Z\\p{javaUpperCase}].*"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static boolean determinerSiNomsEnMAjuscules(String montexte) {
        List<String> lignes = convertirTexteEnListe(montexte);
        boolean res1 = determinerSiNomsEnMAjusculesParPourcentage(lignes);
        if (res1) return res1;
        return determinerSiNomsEnMajusculesParGroupement(lignes);
    }

    private static boolean determinerSiNomsEnMajusculesParGroupement(List<String> lignes) {
        List<String> lignesCommencentParMaj = conserverQueLignesAvecNomsMajusculesAGauche(lignes);
        List<String> onlymaj = lignesCommencentParMaj.stream().map(ActeursAnalyser::recupererPremieresMajuscules).toList();
        Map<String, Long> groups = onlymaj.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));
        List<Long> listOccurences = groups.values().stream().distinct().toList();
        int total = listOccurences.stream().mapToInt(Long::intValue).sum();
        int nombreAuDelaDe20Pourcent=0;
        for(Long occurence:listOccurences){
            if ((Math.toIntExact(occurence)*100/total)>20) nombreAuDelaDe20Pourcent++;
        }
        return nombreAuDelaDe20Pourcent >= 2;
    }

    private static boolean determinerSiNomsEnMAjusculesParPourcentage(List<String> lineStringArray ) {
        boolean res1 = false;
        long countTot = lineStringArray.size();
        List<String> lignesCommencantParMaj =  conserverQueLignesAvecNomsMajusculesAGauche(lineStringArray);
        long nbLignesStartMaj = lignesCommencantParMaj.size();
        System.out.println("Total: "+countTot+ " Aplican: "+nbLignesStartMaj); //TODO: pour Julia. il faudra aussi utiliser un logger grâce à la variable "log"
        double percent =((nbLignesStartMaj*100.0)/countTot);
        if( percent >= 80) res1=true;
        return res1;
    }
}
