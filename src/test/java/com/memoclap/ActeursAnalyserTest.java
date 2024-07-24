package com.memoclap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ActeursAnalyserTest {
    @Test
    public void suppressionCharSpeciaux(){
        List<String> speakers = new ArrayList<>();
        String phrase = "NASTIA (tendant la main) : Eh rends le moi! Fais-pas l'idiot! (La baronne la regarde tout en agitant le livre au-dessus de sa tête.)";
        speakers.add(phrase);
        List<String> res = ActeursAnalyser.suppressionCharSpeciaux(speakers);
        System.out.println(phrase);
        System.out.println(res.getFirst());
        Assertions.assertEquals(1,res.size());
        Assertions.assertEquals("NASTIA (tendant la main) Eh rends le moi! Fais-pas l'idiot! (La baronne la regarde tout en agitant le livre au-dessus de sa tête )",res.getFirst());
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
        nommajuscules.add("LES MAUVAIS ESPRITS");
        nommajuscules.add("LA BARONNE (blalba) hé machin");
        nommajuscules.add("LES BONS");
        nommajuscules.add("NASTIA Ah");
        nommajuscules.add("LE TARTARE Oh");
        nommajuscules.add("LA BARONNE (blalba) hé machin");
        nommajuscules.add("PEPEL (blalba) Hé machin");
        nommajuscules.add("LE SEIGNEUR  Connais-tu Faust ?");
        nommajuscules.add("NASTIA Ah");
        nommajuscules.add("ALBERTO  Hélas !");
        nommajuscules.add("Natalia blab lala bla");
        List<String> res = ActeursAnalyser.conserverDistinctQueNomsMajusculesAGauche(nommajuscules);
        Assertions.assertEquals("[LA BARONNE, NASTIA, LE TARTARE, PEPEL, LE SEIGNEUR, ALBERTO]",res.toString());

    }
    @Test
    public void recupererPremieresMajuscules()
    {
        Assertions.assertEquals( "LKJDSF", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF KSDFLK Lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.recupererPremieresMajuscules("LKJDSF KSDFLK Lskjq lk JAKJS"));
        Assertions.assertEquals("LES MAUVAIS ESPRITS", ActeursAnalyser.recupererPremieresMajuscules("LES MAUVAIS ESPRITS La"));
    }
    @Test
    public void supprimerParenthesesDansParoles(){
        String res = ActeursAnalyser.supprimerParenthesesParole("NASTIA (tendant la main) Eh rends le moi! Fais-pas l'idiot! (La baronne la regarde tout en agitant le livre au-dessus de sa tête )");
        Assertions.assertEquals("NASTIA Eh rends le moi! Fais-pas l'idiot!",res);
    }
    @Test
    public void isChoixNomsEnMajuscules()
    {
        Assertions.assertFalse(ActeursAnalyser.isChoixNomsEnMajuscules(500, 12));
        Assertions.assertTrue(ActeursAnalyser.isChoixNomsEnMajuscules(12, 500));
        Assertions.assertTrue(ActeursAnalyser.isChoixNomsEnMajuscules(182, 578));
        Assertions.assertTrue(ActeursAnalyser.isChoixNomsEnMajuscules(4, 1));
    }
    @Test
    public void suppressionParolesDeNomsMajuscules()
    {
        List<String> listenoms = new ArrayList<>();
        listenoms.add("NASTIA Hé ho");
        listenoms.add("ROSE Hé ho");

        Assertions.assertEquals("NASTIA", ActeursAnalyser.suppressionParolesDeNomsMajuscules(listenoms).getFirst());
    }
    @Test
    public void suppressionParolesDeNomMajuscule(){
        Assertions.assertEquals("NASTIA", ActeursAnalyser.suppressionParolesDeNomMajuscule("NASTIA Hé ho"));
        Assertions.assertEquals("MEPHISTO", ActeursAnalyser.suppressionParolesDeNomMajuscule("MEPHISTO C'est trop tôt"));
        Assertions.assertEquals("MEPHISTO", ActeursAnalyser.suppressionParolesDeNomMajuscule("MEPHISTO C'est chaud ça brûle c'est bouillant !"));
        Assertions.assertEquals( "LKJDSF", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF KSDFLK Lskjq lk"));
        Assertions.assertEquals("LKJDSF KSDFLK", ActeursAnalyser.suppressionParolesDeNomMajuscule("LKJDSF KSDFLK (elle bouffe) Bonjour"));

    }
    @Test
    @Disabled
    public void suppressionCharSpeciauxDesResources(){
        List<String> speakers = new ArrayList<>();
        String filePath = this.getClass().getClassLoader().getResource("suppressionCharSpeciauxDesResources/Dialogue.txt").getPath();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {  //TODO: Julia: je te conseille quand tu commences à avoir une longue methode avec beaucoup de détails techniques de l'extracter. ici par exemple j'extrairais cette methode en l'appelant: "convertFromFileToListOfLines"
            String line;
            while ((line = br.readLine()) != null) {
                speakers.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!speakers.isEmpty()) { //TODO: Julia: tu es dans un test unitaire donc tu n'as pas besoin de cette condition
            List<String> res = ActeursAnalyser.suppressionCharSpeciaux(speakers);
            System.out.println(speakers.getFirst());
            System.out.println(res.getFirst());
            Assertions.assertEquals(1, res.size()); //TODO: Julia: ce test ne test pas assez bien. D'ailleurs la methode devrait s'appeler: lignes_suppressionsCharSpeciauxDesLignes_LignesSansCharsSpeciaux
        } else {
            System.out.println("El archivo está vacío o no se pudo leer.");
        }

    }
@Test
public void MAJUSCULES_getNomsActeursFromText_OnlyMajActors() throws Exception {
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
    LinkedHashMap<Integer, String> res = ActeursAnalyser.getActeursListFromText(montexte);
    System.out.println(res);
    Assertions.assertEquals("{0=ALBERTO, 1=DIANA, 2=LA BARONNE, 3=LA MARIA, 4=LE SEIGNEUR, 5=LE TARTARE, 6=NASTIA, 7=PEPEL, 8=ROBERTO}",res.toString());
}
@Test
public void testReelFichier() throws Exception {
    File originFile = new File("./src/test/resources/faust_Gros_Titres.txt");
    ActeursAnalyser acteursAnalyser = new ActeursAnalyser();
    LinkedHashMap<Integer, String> res = acteursAnalyser.getActeursListFromFile(originFile);
    System.out.println(res);
    Assertions.assertEquals("{0=CHOEUR FINAL, 1=FAUST, 2=LA SORCIERE, 3=LE SEIGNEUR, 4=LES MAUVAIS ESPRITS, 5=LISETTE, 6=MARGUERITE, 7=MARTHE, 8=MEPHISTO, 9=VALENTIN, 10=WAGNER}",res.toString());
}
    @Test
    public void testReel() throws Exception {
        String montexte ="""
                LA CASA DE PAPEL (1)
                Monica- Denver- Berlin
                Intérieur Jour - Couloir de La Fabrique National de la Monnaie et du Timbre de Madrid
                (Monica, une des otages du braquage, a réussi à soustraire le téléphone portable de son chef qui se
                trouvait dans son bureau dans la poche de son manteau. Ce téléphone va lui permettre de rentrer
                en contact avec la police. Denver, un des braqueurs, a un petit faible pour Monica. Il ne sait pas
                que Monica a récupéré le téléphone de son chef. Il l'a fait sortir du bureau où elle et une partie des
                otages sont entassés. Monica est enceinte. Denver lui a promis la pilule du lendemain.
                DENVER - T'as besoin d'argent pour le petit, pas vrai?
                MONICA - Non
                DENVER - Si.
                MONICA – Non, non .
                DENVER – Si. Ecoute-moi, c'est un braquage alors t'as le droit à un petit quelque chose.
                MONICA – Non, ça ira.
                DENVER - Planque ça vite dans tes sous-vêtements. Y a 20 000 balles. Assez pour lui payer des
                couches jusqu'à la fin de ses études.
                MONICA - Merci. (Elle prend l'argent). C'est pas à cause de ça. Je te remercie quand même. C'est
                gentil mais là donne-moi la pilule, je veux retourner avec les autres.
                DENVER – D'accord... Alors c'est quoi? Pourquoi tu veux avorter. Il va te bousiller ta vie.
                MONICA - Qui ?
                DENVER- Ton gosse. Vaut mieux que ce soit lui qui te la bousille ta vie plutôt que n'importe quel
                enfoiré. Ou moi.
                MONICA – Pourquoi toi ?
                DENVER - La vie fait ça très bien sans nous. Un jour, t'es tranquille, au bureau quand débarquent
                des tarés avec des masques de Dali. L'un d'eux perd la tête, se met à tirer et tout part en couille. (il
                lui montre son flingue) Ça, ça bousille la vie. Pas un gosse.
                MONICA – Qu'est-ce que t'en sais de ce qui me gâche la vie? Qu'est ce que t'en sais ?
                DENVER – Ma mère a failli avorter de moi. Mais avant, elle avait inhalé de l'héro qu'elle devait
                vendre pour payer l'avortement. Et sous un porche de merde, elle s'est fait prendre par les keufs. Je
                suis né entre la taule, la drogue et la police. Qu'est-ce que t'en sais ? Parce que j'ai pas l'impression
                que ton boulot soit très excitant et quand tu sors d'ici, ta vie n'a pas l'air aussi rose que ça ? Quoi ?
                Tu fais du « kilates »
                1MONICA – Du Pilates.
                DENVER – Super j'aime encore mieux. Et vendredi soir tu vas boire des coups ? Putain, non, c'est
                chiant, encore un plan gâché par le gosse. Et avec qui tu sors boire des verres ? Elles sont où tes
                amies ? Tu veux que je te le dise. Chez elles, avec leurs gosses. Et leur vie a l'air foutue ? Non, tu
                vois. Qu'est-ce que t'as prévu de faire d'aussi fou qu'un gamin t'empêcherait de faire ? Et tu sais
                quoi, si le père est aussi bête pour ne pas vouloir tout ça, ben encore mieux. T'auras tout l'amour de
                ton gosse rien que pour toi.
                BERLIN- Qu'est ce que tu fais ?
                DENVER – Je lui donne la pilule pour avorter.
                BERLIN- Y a un problème ? Elle a besoin que tu l'aides à l'avaler ?
                DENVER – Avorter c'est une affaire privée, non ? Les femmes décident mais pas devant tout le
                monde. (Denver lui donne la pilule)
                MONICA – Quoi qu'il en soit, je vais réfléchir à la question en attendant qu'on sorte d'ici.
                BERLIN- Fantastique. Je suis content de savoir que tu as décidé de ne pas avorter. Vraiment.
                Retourne maintenant dans le bureau avec les autres.
                MONICA – Ouais.
                (Berlin la laisse passer. Pendant qu'elle s'éloigne, le téléphone bippe.
                Berlin la fouille, retire l'argent de son soutien-gorge,
                BERLIN- Tu sors d'un enterrement de vie de garçon ou tu y allais ?
                … puis le téléphone)
                (en privé à Denver) Denver, Qu'arrivera-t-il à notre autorité quand les autres l'apprendront ?
                Descends-la. Tu m'as entendu.
                (Berlin part. Denver emmène Monica aux toilettes).
                LA CASA DE PAPEL (2)
                Monica- Denver
                Intérieur Jour – Toilettes de La Fabrique National de la Monnaie et du Timbre
                (Monica, une des otages du braquage, a réussi à prendre le téléphone portable de son chef qui se
                trouvait dans la poche de son manteau. Elle s'est fait prendre. Berlin donne l'ordre à Denver de la
                tuer).
                MONICA – Non. Non. Ne fais pas ça. S'il te plait.
                2DENVER – Pourquoi tu as pris ce putain de téléphone ?
                MONICA- Pardon
                DENVER- Pourquoi tu m'as trahi ? Tu sais ce qui va se passer si je ne te tue pas.
                MONICA- Non
                DENVER- Ils me tueront d'abord.
                MONICA- Non
                DENVER- Et après c'est toi qu'ils tueront. Putain de merde ! Merde ! Allez, mec ! Courage !
                Courage !
                DENVER- Mets toi à genoux.
                MONICA – Non
                DENVER- Ne me regarde pas.
                MONICA- Pitié.
                DENVER- Baisse les yeux.
                MONICA- Non, Non
                DENVER- Ne me regarde pas, putain !
                MONICA- Non
                Il tire
                3
                             """;
        LinkedHashMap<Integer, String> res = ActeursAnalyser.getActeursListFromText(montexte);
        System.out.println(res);
        Assertions.assertEquals("{0=1MONICA, 1=2DENVER, 2=BERLIN, 3=DENVER, 4=MONICA}",res.toString());
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
        List<String> res = ActeursAnalyser.conserverDistinctQueNomsMajusculesAGauche(nommajuscules);
        Assertions.assertEquals("[NASTIA, LE TARTARE, LA BARONNE, PEPEL, LE SEIGNEUR]", res.toString());
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
    @Test
    public void vraiTexte_determinerSiNomsEnMajuscules_Oui(){
        String montexte ="""
                LA CASA DE PAPEL (1)
                Monica- Denver- Berlin
                Intérieur Jour - Couloir de La Fabrique National de la Monnaie et du Timbre de Madrid
                (Monica, une des otages du braquage, a réussi à soustraire le téléphone portable de son chef qui se
                trouvait dans son bureau dans la poche de son manteau. Ce téléphone va lui permettre de rentrer
                en contact avec la police. Denver, un des braqueurs, a un petit faible pour Monica. Il ne sait pas
                que Monica a récupéré le téléphone de son chef. Il l'a fait sortir du bureau où elle et une partie des
                otages sont entassés. Monica est enceinte. Denver lui a promis la pilule du lendemain.
                DENVER - T'as besoin d'argent pour le petit, pas vrai?
                MONICA - Non
                DENVER - Si.
                MONICA – Non, non .
                DENVER – Si. Ecoute-moi, c'est un braquage alors t'as le droit à un petit quelque chose.
                MONICA – Non, ça ira.
                DENVER - Planque ça vite dans tes sous-vêtements. Y a 20 000 balles. Assez pour lui payer des
                couches jusqu'à la fin de ses études.
                MONICA - Merci. (Elle prend l'argent). C'est pas à cause de ça. Je te remercie quand même. C'est
                gentil mais là donne-moi la pilule, je veux retourner avec les autres.
                DENVER – D'accord... Alors c'est quoi? Pourquoi tu veux avorter. Il va te bousiller ta vie.
                MONICA - Qui ?
                DENVER- Ton gosse. Vaut mieux que ce soit lui qui te la bousille ta vie plutôt que n'importe quel
                enfoiré. Ou moi.
                MONICA – Pourquoi toi ?
                DENVER - La vie fait ça très bien sans nous. Un jour, t'es tranquille, au bureau quand débarquent
                des tarés avec des masques de Dali. L'un d'eux perd la tête, se met à tirer et tout part en couille. (il
                lui montre son flingue) Ça, ça bousille la vie. Pas un gosse.
                MONICA – Qu'est-ce que t'en sais de ce qui me gâche la vie? Qu'est ce que t'en sais ?
                DENVER – Ma mère a failli avorter de moi. Mais avant, elle avait inhalé de l'héro qu'elle devait
                vendre pour payer l'avortement. Et sous un porche de merde, elle s'est fait prendre par les keufs. Je
                suis né entre la taule, la drogue et la police. Qu'est-ce que t'en sais ? Parce que j'ai pas l'impression
                que ton boulot soit très excitant et quand tu sors d'ici, ta vie n'a pas l'air aussi rose que ça ? Quoi ?
                Tu fais du « kilates »
                1MONICA – Du Pilates.
                DENVER – Super j'aime encore mieux. Et vendredi soir tu vas boire des coups ? Putain, non, c'est
                chiant, encore un plan gâché par le gosse. Et avec qui tu sors boire des verres ? Elles sont où tes
                amies ? Tu veux que je te le dise. Chez elles, avec leurs gosses. Et leur vie a l'air foutue ? Non, tu
                vois. Qu'est-ce que t'as prévu de faire d'aussi fou qu'un gamin t'empêcherait de faire ? Et tu sais
                quoi, si le père est aussi bête pour ne pas vouloir tout ça, ben encore mieux. T'auras tout l'amour de
                ton gosse rien que pour toi.
                BERLIN- Qu'est ce que tu fais ?
                DENVER – Je lui donne la pilule pour avorter.
                BERLIN- Y a un problème ? Elle a besoin que tu l'aides à l'avaler ?
                DENVER – Avorter c'est une affaire privée, non ? Les femmes décident mais pas devant tout le
                monde. (Denver lui donne la pilule)
                MONICA – Quoi qu'il en soit, je vais réfléchir à la question en attendant qu'on sorte d'ici.
                BERLIN- Fantastique. Je suis content de savoir que tu as décidé de ne pas avorter. Vraiment.
                Retourne maintenant dans le bureau avec les autres.
                MONICA – Ouais.
                (Berlin la laisse passer. Pendant qu'elle s'éloigne, le téléphone bippe.
                Berlin la fouille, retire l'argent de son soutien-gorge,
                BERLIN- Tu sors d'un enterrement de vie de garçon ou tu y allais ?
                … puis le téléphone)
                (en privé à Denver) Denver, Qu'arrivera-t-il à notre autorité quand les autres l'apprendront ?
                Descends-la. Tu m'as entendu.
                (Berlin part. Denver emmène Monica aux toilettes).
                LA CASA DE PAPEL (2)
                Monica- Denver
                Intérieur Jour – Toilettes de La Fabrique National de la Monnaie et du Timbre
                (Monica, une des otages du braquage, a réussi à prendre le téléphone portable de son chef qui se
                trouvait dans la poche de son manteau. Elle s'est fait prendre. Berlin donne l'ordre à Denver de la
                tuer).
                MONICA – Non. Non. Ne fais pas ça. S'il te plait.
                2DENVER – Pourquoi tu as pris ce putain de téléphone ?
                MONICA- Pardon
                DENVER- Pourquoi tu m'as trahi ? Tu sais ce qui va se passer si je ne te tue pas.
                MONICA- Non
                DENVER- Ils me tueront d'abord.
                MONICA- Non
                DENVER- Et après c'est toi qu'ils tueront. Putain de merde ! Merde ! Allez, mec ! Courage !
                Courage !
                DENVER- Mets toi à genoux.
                MONICA – Non
                DENVER- Ne me regarde pas.
                MONICA- Pitié.
                DENVER- Baisse les yeux.
                MONICA- Non, Non
                DENVER- Ne me regarde pas, putain !
                MONICA- Non
                Il tire
                3
                             """;
        boolean res1 = ActeursAnalyser.determinerSiNomsEnMAjuscules(montexte);
        Assertions.assertTrue(res1);
    }

    @Test
    public void maj_analyseLineForPossibleActorsName_ret(){

        List<String> liste= new ArrayList<>();
        ActeursAnalyser.fromLineToListOfRedondances(liste, "DENVER- Ne me regarde pas, putain !", ActeursAnalyser.LIGNES_AVEC_NOMS_EN_MAJUSCULES_REGEXP);
        System.out.println(liste.size());
        liste= new ArrayList<>();
        ActeursAnalyser.fromLineToListOfRedondances(liste, "DENVER- Ne me regarde pas, putain !", ActeursAnalyser.LIGNES_AVEC_NOMS_TOUT_TYPE_REGEXP);
        System.out.println(liste.size());

        liste= new ArrayList<>();
        ActeursAnalyser.fromLineToListOfRedondances(liste, "Denver- Ne me regarde pas, putain !", ActeursAnalyser.LIGNES_AVEC_NOMS_EN_MAJUSCULES_REGEXP);
        System.out.println(liste.size());
        liste= new ArrayList<>();
        ActeursAnalyser.fromLineToListOfRedondances(liste, "Denver- Ne me regarde pas, putain !", ActeursAnalyser.LIGNES_AVEC_NOMS_TOUT_TYPE_REGEXP);
        System.out.println(liste.size());
    }
}