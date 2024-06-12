# BookApp

This app is designed to help admin, user manage and discover books, utilizing Firebase as the backend. Below is a list of key files and their functionalities:

## Activity Files

- `AddBookActivity.java`: Manages the process of adding new books to the user's collection.
- `BookDetailActivity.java`: Displays detailed information about a specific book.
- `LoginActivity.java`: Handles user authentication.
- `MainActivity.java`: The app's entry point and primary navigation hub.
- `RegisterActivity.java`: Manages user registration.
- `SplashActivity.java`: Displays a splash screen while the app initializes.
- `UpdateBookActivity.java`: Handles the process of updating book details.

## Fragment Files

- `BookListFragment.java`: Displays a list of books in the user's collection.
- `ProfileFragment.java`: Handles user profile display and editing.
- `SearchFragment.java`: Allows users to search for books.

## Adapter Files

- `BookAdapter.java`: Manages the display of books in a RecyclerView.
- `CategoryAdapter.java`: Manages the display of book categories.

## Model Files

- `Book.java`: Defines the data structure for a book.
- `Category.java`: Defines the data structure for a book category.
- `User.java`: Defines the data structure for a user.

## Utility Files

- `FirebaseUtils.java`: Provides utility functions for interacting with Firebase services.
- `ImageUtils.java`: Handles image processing and uploading.

Feel free to explore these files to understand the structure of the app and how different components interact. The app leverages Firebase Authentication, Realtime Database, and Firebase Storage to provide a seamless book management experience.

## Getting Started

To use this app:

1. Clone or download the repository.
2. Set up your Firebase project and update the `google-services.json` file.
3. Build and run the app on your Android device or emulator.

For any questions or issues, please feel free to open an issue in the repository. Enjoy managing your book collection with BookApp!
