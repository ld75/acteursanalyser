package com.memoclap;

import jakarta.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Dependent
public class ActeursAnalyser {
    static final Logger log = LoggerFactory.getLogger(ActeursAnalyser.class);
    public LinkedHashMap<Integer, String> getActeursListFromText(File text) throws IOException {
        LinkedHashMap<Integer, String> ordreacteurs = new LinkedHashMap<>();
        var content = Files.readString(text.toPath(), Charset.forName("Utf-8"));
        List<String> lignes = Arrays.stream(content.split("\n")).collect(Collectors.toList());
        List<String> speakers = new ArrayList<>();
        for (String ligne:lignes) {
            analyseLineForPossibleActorsName( speakers, ligne);
        }
        speakers = extractionVraisNomsPersonnagesEtNettoyage(speakers);
        int i=0;
        for (String speaker : speakers) {
            log.debug(speaker);
            ordreacteurs.put(i,speaker);
            i++;
        }
        return ordreacteurs;
    }

    private static List<String> extractionVraisNomsPersonnagesEtNettoyage(List<String> speakers) {
        List<String> apparitionsMultiplesSansLesCharsSpecs = suppressionCharSpeciaux(speakers);
        //apparitionsMultiplesSansLesCharsSpecs.stream().forEach(s -> System.out.println("00000000000"+s));
        List<String> listeSansLesFauxNoms = suppressionFauxNoms(apparitionsMultiplesSansLesCharsSpecs);
        boolean choixNomsEnMajuscules = isChoixNomsEnMajuscules(listeSansLesFauxNoms);
        if (choixNomsEnMajuscules)  listeSansLesFauxNoms= suppressionParolesDeNomsMajuscules(listeSansLesFauxNoms);
        List<String> listeSansLesFauxNomsQueRedondants = conservationQueDesMultiplesOccurences(listeSansLesFauxNoms);
        if (choixNomsEnMajuscules) {
            listeSansLesFauxNomsQueRedondants= ajoutDesActeursParlantDeConcertQuneSeuleFois(listeSansLesFauxNoms, listeSansLesFauxNomsQueRedondants);
            listeSansLesFauxNomsQueRedondants= ajoutDesActeursParlantQuneSeuleFois(listeSansLesFauxNoms, listeSansLesFauxNomsQueRedondants);
        }
        return listeSansLesFauxNomsQueRedondants.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> suppressionFauxNoms(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        boolean choixNomsEnMajuscules = isChoixNomsEnMajuscules(apparitionsMultiplesSansLesCharsSpecs);
        List<String> listeSansLesFauxNoms = new ArrayList<>();
        if (choixNomsEnMajuscules) {
            apparitionsMultiplesSansLesCharsSpecs= conserverQueNomsMajusculesAGauche(apparitionsMultiplesSansLesCharsSpecs);
            listeSansLesFauxNoms = suppressionDeNomsContenus(apparitionsMultiplesSansLesCharsSpecs);
        }
        else{
            apparitionsMultiplesSansLesCharsSpecs= conserverQueNomsAMinuscules(apparitionsMultiplesSansLesCharsSpecs);
            listeSansLesFauxNoms = suppressionDeNomsContenants(apparitionsMultiplesSansLesCharsSpecs);
        }
        return listeSansLesFauxNoms;

    }

    private static boolean isChoixNomsEnMajuscules(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        long nombreDeNomsAMajuscules = conserverQueNomsMajusculesAGauche(apparitionsMultiplesSansLesCharsSpecs).size();
        long nombreDeNomsAMinuscules = analyseNombreDeNomsAMinuscules(apparitionsMultiplesSansLesCharsSpecs);
        boolean isChoixNomsEnMajuscules = isChoixNomsEnMajuscules(nombreDeNomsAMajuscules, nombreDeNomsAMinuscules);
        return isChoixNomsEnMajuscules;
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
        List<String> finalSpeakers = listeSansLesFauxNoms;
        finalSpeakers.stream().forEach(s -> log.debug(s));
        return listeSansLesFauxNoms.stream().filter(s -> Collections.frequency(finalSpeakers, s) > 1).collect(Collectors.toList());
    }

    private static List<String> ajoutDesActeursParlantDeConcertQuneSeuleFois(List<String> listeSansLesFauxNoms, List<String> listeSansLesFauxNomsQueRedondants) {
            List<String> listeDePlusieursActeursParlantUneSeuleFois = getListeDePlusieursActeursParlantEnMemeTempsUneSeuleFois(listeSansLesFauxNoms);
            listeSansLesFauxNomsQueRedondants.addAll(listeDePlusieursActeursParlantUneSeuleFois);
            return listeSansLesFauxNomsQueRedondants;
    }

    private static List<String> ajoutDesActeursParlantQuneSeuleFois(List<String> listeSansLesFauxNoms, List<String> listeSansLesFauxNomsQueRedondants) {
        List<String> listeDePlusieursActeursParlantUneSeuleFois = getListeAceursMajusculeParlantUneSeuleFois(listeSansLesFauxNoms);
        listeSansLesFauxNomsQueRedondants.addAll(listeDePlusieursActeursParlantUneSeuleFois);
        return listeSansLesFauxNomsQueRedondants;
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
        listeSansLesFauxNoms.stream().forEach(s-> log.debug(s));
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

    private static void analyseLineForPossibleActorsName(List<String> speakers, String ligne) {
        Pattern pattern = Pattern.compile("^\\S{2,}[\\w\\p{javaUpperCase} .]*[\\w\\W]+");
        Matcher matcher = pattern.matcher(ligne);
        while (matcher.find()) {
            String speaker = matcher.group().trim();
            speakers.add(speaker);
            if (speaker.contains(" ")) {
                String nextOccurence = speaker.substring(0, speaker.lastIndexOf(" ") + 1);
                if (nextOccurence.length()>3) analyseLineForPossibleActorsName(speakers, nextOccurence);
            }
        }

    }

    protected static List<String> suppressionParolesDeNomsMajuscules(List<String> listeSansLesFauxNoms) {
        return listeSansLesFauxNoms.stream()
                .map(s -> suppressionParolesDeNomMajuscule(s))
                .collect(Collectors.toList());
    }

    protected static List<String> conserverQueNomsMajusculesAGauche(List<String> apparitionsMultiplesSansLesCharsSpecs) {
        List<String> nomsAMajuscules = apparitionsMultiplesSansLesCharsSpecs.stream()
                .distinct()
                .filter(s -> nestpasToutMajNiToutEntreParentheses(s))
                .map(s -> supprimerParenthesesParole(s.trim()))
                .filter(s -> isNomSuiviDeParoles(s))
                .map(s -> recupererPremieresMajuscules(s.trim()).trim())
                .filter(s->!s.matches(".*\\s\\p{javaUpperCase}")) //TODO: pourquoi j'ai mis ça ?
                .distinct()
                .collect(Collectors.toList());
        nomsAMajuscules.stream().forEach(s-> log.debug(s));
        return nomsAMajuscules;
    }

    private static boolean isNomSuiviDeParoles(String s) {
        if (s.contains("(")) return false;
        boolean matches = s.matches("^[A-Z0-9\\p{javaUpperCase} ]{3,}[\\w\\W]+");
//		log.debug(s + " " + matches);
        return matches;
    }

    private static boolean nestpasToutMajNiToutEntreParentheses(String s) {
        if (s.matches("^\\(.*$")) return false;
        boolean res = !s.matches("^[A-Z0-9\\s\\W]*$");
        //log.debug(s+" " +res);
        return res;
    }

    public static boolean isPossibleFauxNomEstDansPreviousDoncFaux(String previous, String possibleFauxNom) {
        return previous != null && previous.length() > possibleFauxNom.length() && previous.contains(possibleFauxNom);
    }

    public static boolean isPossibleFauxNomContientPreviousDoncEstFaux(String previous, String possibleFauxNom) {
        return previous != null && possibleFauxNom.length() > previous.length() && possibleFauxNom.contains(previous) && !possibleFauxNom.contains(" et ");
    }

    public static String recupererPremieresMajuscules(String stringTrimme) {
        stringTrimme = stringTrimme.replaceAll("\\(\\w\\)","");
        String out = stringTrimme.replaceAll("(^[A-Z0-9\\p{javaUpperCase} ]{3,})(\\s{1}\\w{1}[\\p{javaLowerCase}\\s]+.*)", "$1");
//		log.debug(out);
        return out;
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
                .map(s -> s.trim())
                .collect(Collectors.toList());
    }
}
