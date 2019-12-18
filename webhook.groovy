// camel-k: language=groovy

from('undertow:http://0.0.0.0:8080/{{secret.path}}')
  .choice()
    .when().simple('${body} != null')
        .setHeader("Subject").simple('${body}')
        .setBody().constant("")
        .to('smtps://smtp.gmail.com')
    .end()
