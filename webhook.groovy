// camel-k: language=groovy

from('undertow:http://0.0.0.0:8080/{{secret.path}}')
  .choice()
    .when().simple('${body} != null')
        .log('Received: ${body}')
        .process("cleanup")
        .log('Transformed: ${body}')
        .setHeader("Subject").simple('${body}')
        .setBody().constant("")
        .to('smtps://smtp.gmail.com')
    .end()


String[] stopPrefixes = [
  "una confezione di", "una scatola di",
  "un pacco di", "dei pacchi di",
  "un po' di", "un po di", "qualche",
  "del", "dello", "della", "dei", "degli", "delle",
  "un", "uno", "una", 
  "il", "lo", "la", "i", "gli", "le"
];

beans {
    cleanup = processor {
      String s = it.in.getBody(String.class)
      s = s.toLowerCase()
      for (stop in stopPrefixes) {
        if (s.startsWith(stop + " ")) {
          s = s.substring(stop.length() + 1);
          break;
        }
      }
      s = s.capitalize()
      it.in.setBody(s)
    }
}
