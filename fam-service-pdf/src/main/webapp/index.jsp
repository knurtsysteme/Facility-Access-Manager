<!DOCTYPE html>
<html>
    <head>
        <title>Facility Access Manager: Webservice PDF</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="robots" content="index,follow">
        <meta name="author" content="info@knurt.de">
        <meta name="application-name" content="Facility Access Manager: Webservice PDF">
        <meta name="description" content="Generate your PDF-Files by adding content to a template">
        <meta name="keywords" content="Facility Access Manager, PDF">
        <link rel="shortcut icon" href="icons/favicon.ico" type="image/x-icon">
        <link rel="icon" href="icons/favicon.ico" type="image/x-icon">
        <style type="text/css">
          body {
            background-color: #FFFFEE
          }
		fieldset {
			border-width: 0px !important;
		}
        </style>
    </head>
    <body>
      <h1>Facility Access Manager: Webservice PDF</h1>
      This Webservice creates pdf files. By now, you can choose an existing pdf as template and put some text contents into it by simply comit a json object.

      <h2>Usage</h2>
      <div>
	      <p>
	      	Send contents in a JSON Object as described below. Use <code>json</code> as a parameter key for the object. Only <code>POST</code>-Requests are accepted.
	      </p>
      </div>

      <h3>Example 1: Hello World</h3>
      <form action="generated.pdf" method="POST">
        <fieldset>
          <textarea rows="10" cols="50" id="json" name="json">{
  "contents": [{
    "text": "Hello World!"
  }]
}</textarea>
<br />
          <button type="submit">Generate and show PDF</button>
        </fieldset>
      </form>

      <h3>Example 2: line feed</h3>
      <div>Use <code>\n</code> for a line feed.</div>
      <form action="generated.pdf" method="POST">
        <fieldset>
          <textarea rows="10" cols="100" id="json" name="json">{
  "contents": [{
    "text": "Hello World!\nHello World!\nHello World!\nHello World!"
  }]
}</textarea>
<br />
          <button type="submit">Generate and show PDF</button>
        </fieldset>
      </form>

      <h3>Example 3: Position and Style</h3>
<div>
      There are some possibilities for text styling and typography.<br />
      Please note: Not all font-families are available.
</div>
      <form action="generated.pdf" method="POST">
        <fieldset>
          <textarea rows="35" cols="100" id="json" name="json">{
  "contents": [
  {
    "text": "A text in red\naligned right\nunderlined and in a Times-Roman",
    "pagenumber": 1,
    "style": 
    {
      "left": 100,
      "bottom": 10,
      "width": 300,
      "height": 500,
      "color": "#FF0000",
      "font-family": "Times-Roman",
      "font-style": "italic",
      "font-size": "20",
      "text-decoration": "underline",
      "font-weight": "bold",
      "text-align": "right"
    }
  },
  {
    "text": "This is another text\nto show, that you can define many content blocks.",
    "pagenumber": 1,
    "style": 
    {
      "left": 10,
      "bottom": 100,
      "width": 300,
      "height": 500,
      "color": "#00CCDD"
    }
  }]
}</textarea>
<br />
          <button type="submit">Generate PDF</button>
        </fieldset>
      </form>
       <h3>Example 4: Choose a template and an id for the filename</h3>
       <div>
       	<p>The parameter <code>templateurl</code> specifies a pdf that is used as template.</p>
       	<p>If you need another filename, you can add your id with <code>customid</code>. Filename is <code>[timestamp]-[#]-[customid].pdf</code></p>
       </div>
      <form action="generated.pdf" method="POST">
        <fieldset>
          <textarea rows="20" cols="120" id="json" name="json">{
  "customid": "foo",
  "templateurl": "http://facility-access-manager.com/files/cloud.pdf",
  "contents": [
  {
	"text": "My Cloud",
    "style": 
    {
      "left": 156,
      "bottom": 400,
      "height": 37,
      "width": 235,
      "font-size": 16,
      "font-weight": "bold",
      "text-align": "center"
    }
  }]
}</textarea>
<br />
          <button type="submit">Generate PDF</button>
        </fieldset>
      </form>
    </body>
</html>