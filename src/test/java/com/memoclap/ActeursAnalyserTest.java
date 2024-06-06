package com.memoclap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ActeursAnalyserTest {
    @Test
    public void suppressionCharSpeciaux(){
        List<String> speakers = new ArrayList<>();

        String phrase = "NASTIA (tendant la main) : Eh rends le moi! Fais-pas l'idiot! (La baronne la regarde tout en agitant le livre au-dessus de sa tête.)";
        speakers.add(phrase);
        List<String> res = ActeursAnalyser.suppressionCharSpeciaux(speakers);
        System.out.println(phrase);
        System.out.println(res.get(0));
        Assertions.assertEquals(1,res.size());
        //Assertions.assertEquals(phrase,res.get(0)); TODO: pour Julia a faire
    }
    @Test
    public void isPossibleFauxNomEstUnFauxNom()
    {
        boolean res = ActeursAnalyser.isPossibleFauxNomEstDansPreviousDoncFaux("LES MAUVAIS ESPRITS", "LES MAUVAIS");
        Assertions.assertTrue(res);
    }
    @Test
    public void conserverQueNomsMajuscules(){
        List<String> nommajuscules = new ArrayList<>();
        nommajuscules.add("LE TARTARE Oh");
        nommajuscules.add("LA BARONNE (blalba) hé machin");
        nommajuscules.add("LES BONS");
        nommajuscules.add("LA BARONNE (blalba) hé machin");
        nommajuscules.add("PEPEL (blalba) Hé machin");
        nommajuscules.add("LE SEIGNEUR  Connais-tu Faust ?");
        nommajuscules.add("NASTIA Ah");
        nommajuscules.add("ALBERTO  Hélas !");
        nommajuscules.add("Natalia blab lala bla");
        List<String> res = ActeursAnalyser.conserverQueNomsMajusculesAGauche(nommajuscules);
        Assertions.assertEquals("[NASTIA, PEPEL, ALBERTO]",res.toString());

    }
    @Test
    public void recupererPremieresMajuscules()
    {
        Assertions.assertEquals( "LKJDSF", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF KSDFLK Lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF KSDFLK Lskjq lk JAKJS"));
    }
    @Test
    public void supprimerParenthesesDansParoles(){
        String res = ActeursAnalyser.supprimerParenthesesParole("NASTIA (tendant la main) Eh rends le moi! Fais-pas l'idiot! (La baronne la regarde tout en agitant le livre au-dessus de sa tête )");
        Assertions.assertEquals("NASTIA Eh rends le moi! Fais-pas l'idiot!",res);
    }
    @Test
    public void isChoixNomsEnMajuscules()
    {
        Assertions.assertEquals(false, ActeursAnalyser.isChoixNomsEnMajuscules(500,12));
        Assertions.assertEquals(true, ActeursAnalyser.isChoixNomsEnMajuscules(12,500));
        Assertions.assertEquals(true, ActeursAnalyser.isChoixNomsEnMajuscules(182,578));
        Assertions.assertEquals(true, ActeursAnalyser.isChoixNomsEnMajuscules(4,1));
    }
    @Test
    public void suppressionParolesDeNomsMajuscules()
    {
        List<String> listenoms = new ArrayList<>();
        listenoms.add("NASTIA Hé ho");
        listenoms.add("ROSE Hé ho");

        Assertions.assertEquals("NASTIA", ActeursAnalyser.suppressionParolesDeNomsMajuscules(listenoms).get(0));
    }
    @Test
    public void suppressionParolesDeNomMajuscule()
    {
        Assertions.assertEquals("NASTIA", ActeursAnalyser.suppressionParolesDeNomMajuscule("NASTIA Hé ho"));
        Assertions.assertEquals("MEPHISTO", ActeursAnalyser.suppressionParolesDeNomMajuscule("MEPHISTO C'est trop tôt"));
        Assertions.assertEquals("MEPHISTO", ActeursAnalyser.suppressionParolesDeNomMajuscule("MEPHISTO C'est chaud ça brûle c'est bouillant !"));
        Assertions.assertEquals( "LKJDSF", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF KSDFLK Lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF KSDFLK (elle bouffe) Bonjour"));

    }
    @Test
    public void suppressionCharSpeciauxDesResources(){
        List<String> speakers = new ArrayList<>();
        String filePath = this.getClass().getClassLoader().getResource("suppressionCharSpeciauxDesResources/Dialogue.txt").getPath();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                speakers.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!speakers.isEmpty()) {
            List<String> res = ActeursAnalyser.suppressionCharSpeciaux(speakers);
            System.out.println(speakers.get(0));
            System.out.println(res.get(0));
            Assertions.assertEquals(1, res.size());
        } else {
            System.out.println("El archivo está vacío o no se pudo leer.");
        }

    }

    @Test
    public void conserverQueNomsMajusculesSansDemstrative() {
        List<String> nommajuscules = new ArrayList<>();
        nommajuscules.add("LES MAUVAIS ESPRITS");
        nommajuscules.add("LES BONS");
        nommajuscules.add("NASTIA Ah");
        nommajuscules.add("LE TARTARE Oh");
        nommajuscules.add("LA BARONNE (blalba) hé machin");
        nommajuscules.add("PEPEL (blalba) Hé machin");
        nommajuscules.add("LE SEIGNEUR  Connais-tu Faust ?");
        nommajuscules.add("NASTIA  Óu est tu Pepel ?");
        List<String> res = ActeursAnalyser.conserverQueNomsMajusculesAGauche(nommajuscules);
        Assertions.assertEquals("[NASTIA, LE TARTARE, LA BARONNE, PEPEL, LE SEIGNEUR, NASTIA]", res.toString());
    }
    @Test
    public void texteAvecNomsEnMajuscules_determinerSiNomsEnMajuscules_NomsEnMajusculesTrue(){ // point 1
        String montexte ="""
                La maria bla bla
                LA MARIA bla bla
                ALBERTO bla bla bla
                LA MARIA ble bla
                ALBERTO ble bla bla                
                maria bla bla bla
                DIANA bla bla bla
                ROBERTO bli bla bla
                LES MAUVAIS ESPRITS
                LA MARIA bli bla
                ALBERTO blo bla bla                
                LES BONS
                NASTIA Ah
                LE TARTARE Oh
                LA BARONNE (blalba) hé machin
                PEPEL (blalba) Hé machin
                LE SEIGNEUR  Connais-tu Faust ?
                NASTIA  Óu est tu Pepel ?
                             """;
            boolean res1 = ActeursAnalyser.determinerSiNomsEnMAjuscules(montexte);
            Assertions.assertTrue(res1);
        }
    @Test
    public void texteAvecNomsEnMinuscules_determinerSiNomsEnMajuscules_NomsEnMajusculesFalse(){ // point 1
        String montexte =  """
                Alberto bla bla bla
                Maria bla bla bla
                Diana bla bla bla
                CATALINA bla ble bli
                Roberto bla bla bla""";


        boolean res1 = ActeursAnalyser.determinerSiNomsEnMAjuscules(montexte);
        Assertions.assertFalse(res1);

    }

}