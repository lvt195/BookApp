# BookApp

This app is designed to help admin, user manage and discover books, utilizing Firebase as the backend. 
Technologies and libraries used: 
- Android: TextView, TabLayout, Floating Widget, ProgressDialog, RecyclerView, Fragment, ViewPager, Adapter,...
- Data Binding
- PDF View: https://github.com/DImuthuUpe/AndroidPdfViewer
- Firebase: Realtime Database, Storage, Authentication
- Glide: https://github.com/bumptech/glide
Below is a list of key files and their functionalities:

## Activity Files

- `CategoryAddActivity.java`: Manages the addition of new categories for admin and including data validation.
- `DashboardAdminActivity.java`: Displays a list of categories, search category and floating buttons add book for admin.
- `DashboardUserActivity.java`: 
Display a list of all books, filter books by most viewed, most downloaded and books by categories
- `ForgetPasswordActivity.java`: Displaying the forgot password screen will enter the recovery email, send an email to that email and including data validation.
- `LoginActivity.java`: Log in by email and password. After authentication, it checks user permissions and redirects to: DashboardUserActivity for users and DashboardAdminActivity for admin.
- `MainActivity.java`: 
MainActivity is the application's start screen. Users can choose to log in or skip to access the user interface..
- `PdfAddActivity.java`: 
Admin adds a book: select the PDF file from the device, select category, after adding the pdf file will be stored on Firebase Storage
- `PdfDetailActivity.java`: Displays detailed information of a Book and allows users to read book, view comments, add/remove book from favorites, and download books.
- `PdfEditActivity.java`:Allows Admin to edit information of a Book, can select a category from the available list and update the information of the Book after editing.
- `PdfListAdminActivity.java`: Displays a list of Books belonging to a specific category. Admin can view this list and search for Books by title. The data is pulled from the Firebase Realtime Database and displayed in a RecyclerView. Users can also return to the previous screen by pressing the "back" button.
- `PdfViewActivity.java`: Allows users to view books as pdf files loaded from a url pointing to books located in Firebase Storage.
- `ProfileActivity.java`: Displays the user's personal information: name, email, profile picture, join date, account type, and email verification statu; check and send verification emails, and view a list of their favorite books.
- `ProfileEditActivity.java`: Allows users to edit their personal information and profile picture. User can enter new name and select image from gallery or camera. After updating the information, the new data will be saved to the database and displayed on the user interface.
- `RegisterActivity.java`: 
Displays information for account registration such as name, email and password. A new account will be created on Firebase Authentication. If successful, the user's information will be saved to the Firebase Realtime Database and they will be redirected to the app's home screen.
- `SplashActivity.java`: Displays a splash screen while the app initializes.

## Fragment Files

- `BookUserFragment.java`: 
Fragment BookUserFragment displays a list of books based on different criteria such as "All", "Most viewed", or "Most downloaded". It also allows users to search for books and filter the results.

## Adapter Files

- `AdapterCategory.java`: Manages the display of books categories in RowCategory.
- `AdapterCategory.java`: Manages the display of book categories.

## Model Files

- `Book.java`: Defines the data structure for a book.
- `Category.java`: Defines the data structure for a book category.
- `User.java`: Defines the data structure for a user.


Feel free to explore these files to understand the structure of the app and how different components interact. The app leverages Firebase Authentication, Realtime Database, and Firebase Storage to provide a seamless book management experience.

## Getting Started

To use this app:

1. Clone or download the repository.
2. Set up your Firebase project and update the `google-services.json` file.
3. Build and run the app on your Android device or emulator.

For any questions or issues, please feel free to open an issue in the repository. Enjoy managing your book collection with BookApp!
