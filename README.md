## ArchitectureComparison

Este projecto trata de una app que al ingresar un nombre usando una API te sugiere una edad posible para alguien con ese nombre. Esto fue hecho como una forma de comparar ciertas formas mas nuevas de algunas formas mas antiguas en base a lo que vi en un par de cursos. Para lo cual tenemos 2 activities (MainActivityNew con las nuevas formas y MainActivityOld con las antiguas) pero el resultado es el mismo, solo la implementacion es diferente. Paso a listar algunas de las diferencias.

# Uso de vistas: Antes
Para acceder a los distintos elementos en pantalla antes se definian variables globales

    private var etName: EditText? = null
    private var btPredictAge: Button? = null
    private var pbLoading: ProgressBar? = null
    private var tvError: TextView? = null
    private var tvAgePredictionName: TextView? = null
    private var tvAgePredictionAge: TextView? = null
    private var clResult: ConstraintLayout? = null

luego se las vinculaba a la vista en el xml

    etName = findViewById(R.id.etName)
    val btPredictAge: Button = findViewById(R.id.btPredictAge)
    pbLoading = findViewById(R.id.pbLoading)
    clResult = findViewById(R.id.clResult)
    tvAgePredictionName = findViewById(R.id.tvAgePredictionName)
    tvAgePredictionAge = findViewById(R.id.tvAgePredictionAge)
    tvError = findViewById(R.id.tvError)

y solo entonces se podia acceder a las views

# Uso de vistas: Ahora
Se usa ViewBinding para acceder a los elementos referenciando sus ids, se crea la variable binding global

    private lateinit var binding: ActivityMainBinding

se la inicializa en el onCreate

    binding = ActivityMainBinding.inflate(layoutInflater)

y luego de eso se puede acceder al elemento desde la variable binding

    binding.tvAgePredictionName.text = state.data.name


# Sobrevivir cambios de configuracion (rotacion del telefono): Antes
Al rotar el telefono los datos en memoria se pierden porque la activity (pantalla) se destruye por lo que era necesario llamar a este metodo y guardarlos en un Bundle

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT_NAME_VALUE, etName?.text.toString())
        outState.putString(TEXT_VIEW_PREVIOUS_NAME, name)
        outState.putInt(TEXT_VIEW_PREVIOUS_AGE, age)
    }

y al recrearse la activity los lee y vuelve a cargar

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        etName?.setText(savedInstanceState.getString(EDIT_TEXT_NAME_VALUE, ""))
        name = savedInstanceState.getString(TEXT_VIEW_PREVIOUS_NAME, "")
        age = savedInstanceState.getInt(TEXT_VIEW_PREVIOUS_AGE, -1)
        btPredictAge?.isEnabled = etName?.text.toString().isNotBlank()
        tvAgePredictionName?.text = name
        tvAgePredictionAge?.text = age.toString()
        clResult?.visibility = if (name.isNotBlank()) View.VISIBLE else View.INVISIBLE
    }

# Sobrevivir cambios de configuracion (rotacion del telefono): Ahora
Este tipo de informacion se mantiene en un viewmodel que se inicializa dentro de la activity en lugar de directamente en la activity y asi sobrevive la recreacion
En el viewmodel tenemos 

    var predictionState = MutableStateFlow<DataState<AgePrediction>>(Idle)

que guarda un posible valor para el nombre y la edad entonces al rotar el telefono esto se mantine y la activity observa por si hay cambios en esa variable

    tvAgePredictionName.text = state.data.name
    tvAgePredictionAge.text = state.data.age.toString()


# Hacer consultas web: Antes
Se podian hacer a mano con clases como AsyncTask y URL("mi url").readText() (ver clase GetRawAgePrediction) y luego parsear los valores de formato json a mano (como en GetAgePredictionJsonData)

# Hacer consultas web: Ahora
Se usan librerias para hacer las consultas (generalmente retrofit) usando corutinas en las cuales solo configuras una variable y url principal

    val baseUrl = "https://api.agify.io/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }

y luego un metodo que consulta por el endpoint especifico en el que usas una clase para constuir la respuesta.
Instancia de la clase que se usa para las consultas:

    private var service = RetrofitHelper.getInstance().create(AgePredictionApi::class.java)

llamada al metodo:

    viewModelScope.launch(Dispatchers.IO) {
        val result = service.getAgePrediction(currentText.value)
        withContext(Dispatchers.Main) {
            ...
        }

el metodo que hace la consulta:

    @GET("/")
    suspend fun getAgePrediction(@Query("name") name: String) : Response<AgePrediction>

la clase que representa una respuesta:

    data class AgePrediction(
        var name: String,
        var age: Int
    )
