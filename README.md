# PlaceAutoCompleteFragment
Add this to your project to add place autocomplete functionality for example : searching places, entering destination, etc.

# Usage

<i>For a working implementation of this project see the /app folder </i>
<ol>
  <li>
    <p>Include the library as local library project or add this dependency in your build.gradle.</p>
    <pre>
      <code>
        dependencies {
          compile 'com.outlander.placeautocomplete:PlaceAutoComplete:1.0.0'
        }
      </code>
    </pre>
  </li>
  <li>
    <p>Add a PlaceAutoComplete fragment container <i>FrameLayout</i> to your corresponding activity's xml.</p>
    <pre>
      <code>
        &ltFrameLayout
          android:id="@+id/placeautofragment_container"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
        /&gt
      </code>
    </pre>
  </li>
  <li>
    <p>Initialize the PlaceAutoCompleteFragment in the onCreate of your activity.</p>
    <pre>
      <code>
        PlaceAutoCompleteFragment placeAutoCompleteFragment = PlaceAutoCompleteFragment.getInstance(getBaseContext(), true, false);
        placeAutoCompleteFragment.addOnPlaceSelectedListener(new PlaceAutoCompleteFragment.IOnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(PlaceData placeData) {
                //TODO: Add your action
            }

            @Override
            public void onErrorOccurred() {
              //TODO: Alert user
            }
        });
      </code>
    </pre>
  </li>
  <li>
    <p>Call the addFragment method to add the PlaceAutoCompleteFragment to the container.</p>
    <pre>
      <code>    
        private void addFragment(Fragment fragment) {
          FragmentManager fragmentManager = getSupportFragmentManager();
          FragmentTransaction transaction = fragmentManager.beginTransaction();
          transaction.add(R.id.container, fragment);
          transaction.addToBackStack("Any random string or even null will work");
          transaction.commit();
        }
      </code>
    </pre>
  </li>
  <li>
    <p>Override the onBackPressed of the acitivty to manage your fragments accordingly. Below is a basic snippet</p>
    <pre>
      <code>    
        @Override
        public void onBackPressed() {
            if (getSupportFragmentManager().findFragmentById(R.id.container) != null) {
                getSupportFragmentManager().popBackStackImmediate();
            } else {
                super.onBackPressed();
            }
        }
      </code>
    </pre>
  </li>
</ol>

<p>Ready to go!</p>

#Developed by
<ul>
  <li>
    <a href="https://github.com/outlander24">Ashish Totla</a>
  </li>
</ul>

<h3> 
Credits
</h3>
<p>Google PlaceAutoCompleteFragment</p>
