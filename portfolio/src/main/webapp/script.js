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


/* Fetches login status, updating login/logout button */
function getLoginStatus(){
    fetch("/status").then(response => response.text()).then((status) => {

        const loginContainer= document.getElementById('login-container');
        loginContainer.innerText= status; 
    });
}

/* Allows user to login/logout */
function logInOrOut(){
    location.replace("/login");
}

/* On load functions for index and images html pages */
function onLoadIndexImagesPage(){
    getLoginStatus();
}

/* On load functions for interactive html page */
function onLoadInteractivePage(){
    getLoginStatus();
    getFakeComments();
}

/* On load functions for comments html page */
function onLoadCommentsPage(){
    getLoginStatus();
    getComments();
}

/* Fetches random quote */
function getRandomQuote(){
    const requestPromise= fetch('/random-quote');

    requestPromise.then(handleReponse);
}

/* Handles the text promise */
function handleReponse(reponse){
    textPromise= reponse.text();

    textPromise.then(addQuoteToDOM);
}

/* Adds the random quote to the DOM */
function addQuoteToDOM(quote){
    const quoteContainer= document.getElementById('quote-container');
    quoteContainer.innerText= quote;
} 

/* Fetches form html and adds it to DOM */
function getForm() {
  fetch('/new-comment').then(response => response.text()).then((form) => {

    const cardContainer = document.getElementById('card');
    cardContainer.innerHTML= form;   
  });
}

/* Fetches fake comments and adds them to DOM */
function getFakeComments(){
    fetch("/data").then(response => response.json()).then((comment) => {
        
        const commentContainer= document.getElementById('comment-container');
        commentContainer.innerText= '';
        commentContainer.appendChild(createHeaderElement(comment.userID + ", " + comment.submitTime));
        commentContainer.appendChild(createParaElement(comment.comment));
    });
}

/* Fetches comments and adds them to DOM */
function getComments() {
  fetch('/display-comments').then(response => response.json()).then((comments) => {

    const commentContainer = document.getElementById('comment-container');
    commentContainer.innerText= '';    

    comments.forEach((comment) => {
        commentContainer.appendChild(createCommentElement(comment))
    });
  });
}

/* Creates the comment element with delete button */
function createCommentElement(comment){

    const commentElement= document.createElement('span');
    commentElement.className= 'comment';

    const headerElement= createHeaderElement(comment.userID + ", " + comment.submitTime);

    const contentElement= createParaElement(comment.comment);

    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.className= 'r_button'
    deleteButtonElement.innerText = 'Delete';
    deleteButtonElement.addEventListener('click', () => {
        deleteComment(comment);

        commentElement.remove();
    });

    commentElement.appendChild(headerElement);
    commentElement.appendChild(contentElement);
    commentElement.appendChild(deleteButtonElement);
    return commentElement;
}

/* Creates header part of comment object */
function createHeaderElement(text) {
  const hElement = document.createElement('h2');
  hElement.innerText = text;
  return hElement;
}

/* Creates comment part of comment object */
function createParaElement(text) {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}

/* Sends POST request to delete comments */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}