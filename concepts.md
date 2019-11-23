# Threading & UI

so a common thing is that if you work in any UI framework, most functions on a "view" is UI threaded, (unless specifically stated)
eg for android
```kotlin
class Test: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //ui thread
    }
    
    @WorkerThread
    fun doSomethingInNetwork(){
        //do io
       runOnUiThread{
            //update ui
            updateUI()
            imGoodAtAnnotating()
       }       
    }
    //This is implicit @InUi / @UiThread
    fun updateUI(){

    }
    @InUi
    fun imGoodAtAnnotating(){
    
    }

}
```

This shows with the comments, where the expectations are.
So if we "specially" mark the type "Activity" as a Ui something (UiContainer ?inUi maybe even ?)
then all things in that "automagically" gets the inUI annotation.
this means that we can understand something as strange as trampoline functions without a full SA scan / tracking.
eg
```kotlin
class Test: Activity() {
    //implicit: UI thread
    override fun onCreate(savedInstanceState: Bundle?) {
        trampoline()
    }
    
    @WorkerThread
    fun doSomethingInNetwork(){
        //do io
    }
    //implicit: UI thread
    fun trampoline(){
        //ui to background -> NO
        doSomethingInNetwork()
    }
}
```
(the joke here is that android studio will not detect this in the "regular running" inspector; even though its so simple, and that is a full scanner SA implementation....)
It will work until the workerthread annotaion is removed. in that regard external annotations of libraries could prove valuable to share the "details" of say http libraries ect.


as a side note: any function without annotations that violates the no UI <-> Background can have multiple quickfixes. 
 - mark as inUI and launch in background 
 - mark as inBackground and launch in ui 
 - wrap code in a launch without annotation ? 