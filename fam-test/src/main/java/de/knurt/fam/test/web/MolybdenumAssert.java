package de.knurt.fam.test.web;

import de.knurt.fam.template.util.TemplateHtml;

public class MolybdenumAssert {
  /** one and only instance of MolybdenumAssert */
  private volatile static MolybdenumAssert me;

  /** construct MolybdenumAssert */
  private MolybdenumAssert() {
  }

  /**
   * return the one and only instance of MolybdenumAssert
   * 
   * @return the one and only instance of MolybdenumAssert
   */
  public static MolybdenumAssert getInstance() {
    if (me == null) {
      // ↖ no instance so far
      synchronized (MolybdenumAssert.class) {
        if (me == null) {
          // ↖ still no instance so far
          // ↓ the one and only me
          me = new MolybdenumAssert();
        }
      }
    }
    return me;
  }

  /**
   * short for {@link #getInstance()}
   * 
   * @return the one and only instance of MolybdenumAssert
   */
  public static MolybdenumAssert me() {
    return getInstance();
  }

  public String page(String resourceName) {
    String target = String.format("css=body#body_%s", resourceName);
    return String.format(Molybdenum.command2format, "assertElementPresent", target, "", String.format("is %s page", resourceName));
  }

  public String page(String resourceName, String queryParams) {
    return String.format(Molybdenum.command2format, "assertLocation", TemplateHtml.me().getHref(resourceName) + queryParams, "", "assert location with query params " + resourceName);
  }

  public String notpage(String resourceName) {
    String target = String.format("css=body#body_%s", resourceName);
    return String.format(Molybdenum.command2format, "assertElementNotPresent", target, "", String.format("is not %s page", resourceName));
  }

}
