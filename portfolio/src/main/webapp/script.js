// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random quote to the page.
 */
function addRandomQuote() {
  const quotes =
      ['Batman: (to Owlman) There is a difference between you and me. We both looked into the abyss, but when it looked back at us, you blinked.', 
      'Morpheus: There is a difference between knowing the path and walking the path.', 
      'Chewbacca: RRWWWGG.', 
      'Cobb: If you\'re going to perform inception, you need imagination.'];
 
  // Pick a random quote.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];
 
  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function getRandomQuote(){
    console.log('Fetching a random quote..');

    const requestPromise= fetch('/random-quote');

    requestPromise.then(handleReponse);
}

function handleReponse(reponse){
    console.log('Handling response..');

    textPromise= reponse.text();

    textPromise.then(addQuoteToDOM);
}

function addQuoteToDOM(quote){
    console.log('Adding quote to DOM: ' + quote);

    const quoteContainer= document.getElementById('quote-container');
    quoteContainer.innerText= quote;
}   

function getFakeComments(){
    fetch("/data").then(response => response.json()).then((comment) => {
        
        console.log(comment);
        const commentContainer= document.getElementById('comment-container');
        commentContainer.innerText= '';
        commentContainer.appendChild(createHeaderElement(comment.userID + ", " + comment.submitTime));
        commentContainer.appendChild(createParaElement(comment.comment));
    });
}

function getAllComments() {
  fetch('/comments').then(response => response.json()).then((comments) => {

    const commentContainer = document.getElementById('comment-container');
    commentContainer.innerText= '';    
    console.log(comments);

    comments.forEach((comment) => {
        console.log(comment);  
        commentContainer.appendChild(createHeaderElement(comment.userID + ", " + comment.submitTime));
        commentContainer.appendChild(createParaElement(comment.comment));
    });
  });
}

function createHeaderElement(text) {
  const hElement = document.createElement('h2');
  hElement.innerText = text;
  return hElement;
}

function createParaElement(text) {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}


