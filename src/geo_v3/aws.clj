(ns geo-v3.aws
  (:import
   (software.amazon.awssdk.auth.credentials
     AwsBasicCredentials
     StaticCredentialsProvider)
   (software.amazon.awssdk.core
     ResponseInputStream)
   (software.amazon.awssdk.core.sync
     RequestBody)
   (software.amazon.awssdk.regions
     Region)
   (software.amazon.awssdk.services.s3
     S3Client)
   (software.amazon.awssdk.services.s3.model
    GetObjectRequest
    GetObjectResponse
    HeadObjectRequest
    HeadObjectResponse
    ObjectCannedACL
    PutObjectRequest)))

(defn get-meta [^S3Client s3 bucket key]
  (let [req ^HeadObjectRequest (-> (HeadObjectRequest/builder)
                                   (.bucket bucket)
                                   (.key key)
                                   (.build))
        res ^HeadObjectResponse (.headObject s3 req)]
    {:content-type   (.contentType res)
     :content-length (.contentLength res)
     :etag           (.eTag res)}))

(defn get-object [^S3Client s3 bucket key]
  (let [stream          ^ResponseInputStream (.getObject s3 (-> (GetObjectRequest/builder)
                                                                (.bucket bucket)
                                                                (.key key)
                                                                (.build)))
        object-response ^GetObjectResponse (.response stream)]
    {:content-type   (.contentType object-response)
     :content-length (.contentLength object-response)
     :etag           (.eTag object-response)
     :stream         stream}))

(def ^:private supported-acl-value? #{"private"
                                      "public-read"
                                      "public-read-write"
                                      "autheticated-read"
                                      "bucket-owner-read"
                                      "bucket-owner-full-control"})

(defn- builder-set-bucket-key [builder bucket key]
  (-> builder
      (.bucket bucket)
      (.key key)))

(defn- builder-set-s3-opts [builder bucket key opts]
  (when-let [acl (:acl opts)]
    (when-not (supported-acl-value? acl)
      (throw (ex-info (str "unsupported acl value: " acl) {:acl acl}))))

  (-> builder
      (builder-set-bucket-key bucket key)
      (cond->
       (:acl opts) (.acl (ObjectCannedACL/fromValue (:acl opts)))
       (:content-disposition opts) (.contentDisposition (:content-disposition opts))
       (:content-encoding opts) (.contentEncoding (:content-encoding opts))
       (:content-language opts) (.contentLanguage (:content-language opts))
       (:content-length opts) (.contentLength (long (:content-length opts)))
       (:content-MD5 opts) (.contentMD5 (:content-MD5 opts))
       (:content-type opts) (.contentType (:content-type opts))
       (:metadata opts) (.metadata (:metadata opts)))))

(defn- data->request-body [data opts]
  (cond
    (string? data) (RequestBody/fromString data)
    (bytes? data) (RequestBody/fromBytes data)
    (instance? java.io.File data) (RequestBody/fromFile data)
    (instance? java.io.InputStream data) (RequestBody/fromInputStream
                                          data
                                          (:content-length opts))))

(defn- close-data [data]
  (when (instance? java.io.InputStream data)
    (.close data))
  nil)

(defn put-object [^S3Client s3 bucket key data opts]
  (let [request  (-> (PutObjectRequest/builder)
                     (builder-set-s3-opts bucket key opts)
                     (.build))
        body     (data->request-body data opts)
        response (.putObject s3 request body)]
    (close-data body)
    (.eTag response)))

(defn s3-client [{:keys [access-key secret-key endpoint region
                         use-arn-region-enabled force-path-style
                         accelerate]
                  :or {use-arn-region-enabled false
                       force-path-style false
                       accelerate false}}]
  (let [credentials-provider (when (and access-key secret-key)
                               (-> (AwsBasicCredentials/create access-key secret-key)
                                   (StaticCredentialsProvider/create)))]
    (cond-> (S3Client/builder)
             ;; this needs to be first
      true (.forcePathStyle force-path-style)
      endpoint (.endpointOverride (java.net.URI. endpoint))
      region (.region (Region/of region))
      credentials-provider (.credentialsProvider credentials-provider)
      true (->
            (.serviceConfiguration (reify java.util.function.Consumer
                                     (accept [_ builder]
                                       (-> builder
                                           (.useArnRegionEnabled use-arn-region-enabled)
                                           (.accelerateModeEnabled accelerate)))))
            (.build)))))

(comment

  (def bucket "lemonpi-test-geo-location-v2")

  (def object-key-mmdb "GeoLite2-City.mmdb")

  (def client (s3-client {}))

  (get-object client bucket object-key-mmdb)

  (get-meta client bucket object-key-mmdb)

  ,)
