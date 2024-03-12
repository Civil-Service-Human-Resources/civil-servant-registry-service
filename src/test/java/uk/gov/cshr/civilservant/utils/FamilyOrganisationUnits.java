package uk.gov.cshr.civilservant.utils;

import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FamilyOrganisationUnits {

    private static int COUNTER = 0;

    private static String GODFATHERS_CODE = "gf";

    private List<OrganisationalUnit> family;

    private List<OrganisationalUnit> outsiders;

    public FamilyOrganisationUnits() {
        this.family = buildLargeFamilyOfOrganisationalUnits();
        this.outsiders = buildOutsiders();
    }

    public List<OrganisationalUnit> getFamily() {
        return family;
    }

    public List<OrganisationalUnit> getOutsiders() {
        return outsiders;
    }

    public OrganisationalUnit getTopParent(){
        Optional<OrganisationalUnit> matchingObject = this.family.stream().
                filter(ou -> GODFATHERS_CODE.equals(ou.getCode())).findFirst();
        if(matchingObject.isPresent()) {
            return matchingObject.get();
        }
        return null;
    }

    public List<OrganisationalUnit> getParentsChildren(){
        return this.family.get(0).getChildrenAsList();
    }

    public List<OrganisationalUnit> getParentsChildrenChildren(int godFatherChildIndex){
        if(getParentsChildren() == null) {
            return new ArrayList<OrganisationalUnit>();
        }

        return getParentsChildren().get(godFatherChildIndex).getChildrenAsList();
    }

    private List<OrganisationalUnit> buildOutsiders() {
        List<OrganisationalUnit> outsiders = new ArrayList<>();
        OrganisationalUnit notRelated0 = new OrganisationalUnit();
        notRelated0.setId(200l);
        notRelated0.setName("outsider0");
        notRelated0.setAbbreviation("O0");
        notRelated0.setCode("OUT0");
        OrganisationalUnit notRelated1 = new OrganisationalUnit();
        notRelated1.setId(201l);
        notRelated1.setName("outsider1");
        notRelated1.setAbbreviation("O1");
        notRelated1.setCode("OUT1");
        OrganisationalUnit notRelated2 = new OrganisationalUnit();
        notRelated2.setId(202l);
        notRelated2.setName("outsider2");
        notRelated2.setAbbreviation("O2");
        notRelated2.setCode("OUT2");
        OrganisationalUnit notRelated3 = new OrganisationalUnit();
        notRelated3.setId(203l);
        notRelated3.setName("outsider3");
        notRelated3.setAbbreviation("O3");
        notRelated3.setCode("OUT3");
        outsiders.add(notRelated0);
        outsiders.add(notRelated1);
        outsiders.add(notRelated2);
        outsiders.add(notRelated3);
        return outsiders;
    }

    private static List<OrganisationalUnit> buildLargeFamilyOfOrganisationalUnits() {
        // the family entirely
        List<OrganisationalUnit> theFamily = new ArrayList<>();

        OrganisationalUnit headOfFamily = new OrganisationalUnit();
        headOfFamily.setCode(GODFATHERS_CODE);
        headOfFamily.setParent(null);
        headOfFamily.setAbbreviation(GODFATHERS_CODE.toUpperCase());
        headOfFamily.setName("Godfather: the head of the family");
        headOfFamily.setId(new Long(COUNTER));
        COUNTER++;
        // godfathers children - first level
        List<OrganisationalUnit> godfathersChildren = buildGodFathersChildren();
        headOfFamily.setChildren(godfathersChildren);
        // set parent of godfathers children to be the godfather
        headOfFamily.getChildren().forEach(c -> c.setParent(headOfFamily));

        theFamily.add(0, headOfFamily);

        // godfathers child one children - second level
        List<OrganisationalUnit> godfathersChildOneChildren = buildGodFathersChildOneChildren();
        headOfFamily.getChildrenAsList().get(1).setChildren(godfathersChildOneChildren);
        // set the parent of godfathersChildOneChildren to be godfathersChildOne
        headOfFamily.getChildrenAsList().get(1).getChildren().forEach(c -> c.setParent(headOfFamily.getChildrenAsList().get(1)));

        // godfathers child two children - second level
        List<OrganisationalUnit> godfathersChildTwoChildren = buildGodFathersChildTwoChildren();
        headOfFamily.getChildrenAsList().get(2).setChildren(godfathersChildTwoChildren);
        // set the parent of godfathersChildTwoChildren to be godfathersChildTwo
        headOfFamily.getChildrenAsList().get(2).getChildren().forEach(c -> c.setParent(headOfFamily.getChildrenAsList().get(2)));

        return theFamily;
    }

    private static List<OrganisationalUnit> buildGodFathersChildren(){
        List<OrganisationalUnit> godfathersChildren = new ArrayList<>();
        for(int i=0; i<5; i++) {
            godfathersChildren.add(i, buildChild("god", i, "godfathers"));
        }
        return godfathersChildren;
    }

    private static List<OrganisationalUnit> buildGodFathersChildOneChildren(){
        List<OrganisationalUnit> godFatherChild1Children = new ArrayList<>();
        for(int i=0; i<5; i++) {
            godFatherChild1Children.add(i, buildChild("grandOne", i, "god1"));
        }
        return godFatherChild1Children;
    }

    private static List<OrganisationalUnit> buildGodFathersChildTwoChildren(){
        List<OrganisationalUnit> godFatherChild2Children = new ArrayList<>();
        for(int i=0; i<5; i++) {
            godFatherChild2Children.add(i, buildChild("grandTwo", i, "god2"));
        }
        return godFatherChild2Children;
    }

    private static OrganisationalUnit buildChild(String code, int index, String name) {
        OrganisationalUnit child = new OrganisationalUnit();
        child.setCode(code + index);
        child.setAbbreviation(code.toUpperCase() + index);
        child.setName("child " + index + " of the " + name);
        child.setId(new Long(COUNTER));
        COUNTER++;
        return child;
    }

}
