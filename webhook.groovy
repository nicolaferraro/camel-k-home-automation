// camel-k: language=groovy property-file=application.properties secret=azure

from('undertow:http://0.0.0.0:8080/{{secret.path}}')
  .choice()
    .when().simple('${body} != null')
        .log('Received: ${body}')
        .process("cleanup")
        .log('Transformed: ${body}')
        .to("direct:addTask")
        .setBody().constant("")
    .end()

from('direct:addTask')
  .removeHeaders('*')
  .bean(this, 'create(${body})')
  .marshal().json()
  .setHeader(Exchange.HTTP_METHOD, constant("POST"))
  .setHeader("Authorization", constant("Bearer {{secret:azure/token}}"))
  .to('https://graph.microsoft.com/beta/me/todo/lists/{{todo.list}}/tasks')
  .log('${body}')


def create(String title) {
  return new Task(title)
}

class Task {
  String title
  Body body

  Task(title) {
    this.title = title
    this.body = new Body()
  }

}

class Body {
  String content
  String contentType

  Body() {
    this.content = ""
    this.contentType = "text"
  }
  
}

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
